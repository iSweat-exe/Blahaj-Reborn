package com.blahaj.voidhaj;

import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.util.GeckoLibUtil;
import com.blahaj.BlahajMod;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * The black hole: grows while pulling in every entity except players,
 * collapses, then explodes, destroying everything it swallowed.
 */
public class BlackHoleEntity extends Entity implements GeoEntity {
    public static final int GROW_TICKS = 200;  // 10 s
    public static final int COLLAPSE_TICKS = 60;  // 3 s
    public static final float START_RADIUS = 0.25F;
    public static final float MAX_RADIUS = 4.0F;
    public static final byte EXPLODE_EVENT = 77;

    /** Rips surface blocks out of the ground while growing (and flings them on explosion). */
    public static final boolean SUCK_BLOCKS = true;
    /** Carves a crater under the explosion with a scorched rim. */
    public static final boolean CRATER = true;
    public static final int MAX_SUCKED_BLOCKS = 260;
    public static final int CRATER_RADIUS = 7;

    private static final EntityDataAccessor<Float> RADIUS = SynchedEntityData.defineId(BlackHoleEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> PHASE = SynchedEntityData.defineId(BlackHoleEntity.class, EntityDataSerializers.INT);

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    /** Swallowed entities (server) -> {angle, relative orbit radius, relative height}. */
    private final Map<UUID, float[]> captured = new HashMap<>();
    private int suckedBlocks;
    /** Previous tick's radius, for render interpolation (client). */
    public float prevRadius = START_RADIUS;

    public BlackHoleEntity(EntityType<? extends BlackHoleEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    public float getRadius() {
        return entityData.get(RADIUS);
    }

    public float getRadius(float partialTick) {
        return Mth.lerp(partialTick, prevRadius, getRadius());
    }

    /** True during the final 3 seconds (collapse). */
    public boolean isCollapsing() {
        return entityData.get(PHASE) == 1;
    }

    /** Pull range in blocks. */
    public double pullRange() {
        return 10 + getRadius() * 7;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(RADIUS, START_RADIUS);
        builder.define(PHASE, 0);
    }

    @Override
    public void tick() {
        prevRadius = getRadius();
        super.tick();

        if (level().isClientSide()) {
            clientParticles();
            VoidClientHooks.blackHoleTick.accept(this);
            return;
        }

        ServerLevel server = (ServerLevel) level();
        int age = this.tickCount;

        if (age <= GROW_TICKS) {
            float t = age / (float) GROW_TICKS;
            entityData.set(RADIUS, START_RADIUS + (MAX_RADIUS - START_RADIUS) * (float) Math.pow(t, 1.6));
        } else {
            if (!isCollapsing()) {
                entityData.set(PHASE, 1);
                server.playSound(null, getX(), getY(), getZ(), VoidContent.COLLAPSE, SoundSource.HOSTILE, 8.0F, 1.0F);
                server.playSound(null, getX(), getY(), getZ(), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.HOSTILE, 4.0F, 0.5F);
            }
            float p = Mth.clamp((age - GROW_TICKS) / (float) COLLAPSE_TICKS, 0, 1);
            entityData.set(RADIUS, Math.max(0.12F, MAX_RADIUS * (1 - (float) Math.pow(p, 2.2))));
        }

        pullEntities(server);
        if (SUCK_BLOCKS && !isCollapsing() && age > 30) {
            suckBlocks(server);
        }

        if (age % 40 == 0 && !isCollapsing()) {
            server.playSound(null, getX(), getY(), getZ(), SoundEvents.PORTAL_TRIGGER, SoundSource.HOSTILE, 2.0F, 0.4F + getRadius() * 0.05F);
        }

        if (age >= GROW_TICKS + COLLAPSE_TICKS) {
            explode(server);
        }
    }

    private void pullEntities(ServerLevel server) {
        Vec3 center = position();
        double range = pullRange();
        float radius = getRadius();
        double force = isCollapsing() ? 1.9 : 1.0;

        List<Entity> targets = server.getEntities(this, new AABB(center, center).inflate(range), this::canPull);
        for (Entity e : targets) {
            Vec3 toCenter = center.subtract(e.position().add(0, e.getBbHeight() / 2, 0));
            double dist = toCenter.length();
            if (dist > range) continue;

            if (dist < radius + 0.6) {
                swallow(server, e);
                continue;
            }

            Vec3 dir = toCenter.normalize();
            Vec3 swirl = new Vec3(-dir.z, 0, dir.x).scale(0.45);  // spiral motion
            double accel = Mth.clamp((0.6 + radius) * 0.9 / (dist + 1) * force, 0.02, 0.7);
            Vec3 v = e.getDeltaMovement().scale(0.9).add(dir.add(swirl).scale(accel));
            if (v.length() > 1.8) v = v.normalize().scale(1.8);
            e.setDeltaMovement(v);
            e.needsSync = true;
            e.resetFallDistance();
        }

        // Swallowed entities orbit in the accretion disc and shrink with it;
        // living creatures shrink as well (spaghettification).
        captured.entrySet().removeIf(entry -> {
            Entity e = server.getEntity(entry.getKey());
            if (e == null || e.isRemoved()) return true;
            float[] orbitData = entry.getValue();
            double orbit = Math.max(0.3, radius * orbitData[1]);
            orbitData[0] += (float) (0.9 / Math.max(1.0, orbit));  // closer = faster
            float angle = orbitData[0];
            e.setPos(center.x + Math.cos(angle) * orbit,
                    center.y - e.getBbHeight() / 2 + Math.sin(angle) * orbit * 0.25 + orbitData[2] * radius,
                    center.z + Math.sin(angle) * orbit);
            e.setDeltaMovement(Vec3.ZERO);
            e.needsSync = true;
            if (e instanceof LivingEntity living) {
                AttributeInstance scale = living.getAttribute(Attributes.SCALE);
                if (scale != null) scale.setBaseValue(Math.max(0.0625, scale.getBaseValue() * 0.96));
            }
            return false;
        });
    }

    private boolean canPull(Entity e) {
        if (e instanceof Player || e.isSpectator() || e instanceof BlackHoleEntity || e instanceof RisingVoidBlahaj) return false;
        if (e.hasPassenger(p -> p instanceof Player)) return false;
        return e.isAlive() && !captured.containsKey(e.getUUID());
    }

    private void swallow(ServerLevel server, Entity e) {
        server.sendParticles(ParticleTypes.REVERSE_PORTAL, e.getX(), e.getY() + e.getBbHeight() / 2, e.getZ(), 25, 0.2, 0.2, 0.2, 0.3);
        if (e instanceof ItemEntity || e instanceof ExperienceOrb || e instanceof Projectile) {
            e.discard();  // small objects vanish immediately
            return;
        }
        e.setNoGravity(true);
        captured.put(e.getUUID(), new float[]{random.nextFloat() * Mth.TWO_PI, 1.3F + random.nextFloat() * 1.3F, (random.nextFloat() - 0.5F) * 0.3F});
    }

    /** Rips surface blocks around the black hole and turns them into falling blocks that get pulled in. */
    private void suckBlocks(ServerLevel server) {
        float radius = getRadius();
        int perTick = (int) (radius * 0.7F) + (random.nextFloat() < radius % 1 ? 1 : 0);
        for (int i = 0; i < perTick && suckedBlocks < MAX_SUCKED_BLOCKS; i++) {
            double a = random.nextDouble() * Math.PI * 2;
            double d = random.nextDouble() * (3 + radius * 3.5);
            int x = Mth.floor(getX() + Math.cos(a) * d);
            int z = Mth.floor(getZ() + Math.sin(a) * d);
            BlockPos pos = new BlockPos(x, server.getHeight(Heightmap.Types.WORLD_SURFACE, x, z) - 1, z);
            if (getY() - pos.getY() > 32 || pos.getY() >= getY()) continue;
            if (!canRip(server, pos)) continue;

            FallingBlockEntity block = FallingBlockEntity.fall(server, pos, server.getBlockState(pos));
            block.setDeltaMovement(random.nextGaussian() * 0.05, 0.45 + random.nextDouble() * 0.35, random.nextGaussian() * 0.05);
            block.needsSync = true;
            suckedBlocks++;
            if (random.nextFloat() < 0.3F) {
                server.sendParticles(ParticleTypes.REVERSE_PORTAL, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, 6, 0.3, 0.1, 0.3, 0.05);
            }
        }
    }

    /** Rippable block: not air, not fluid, not unbreakable, not a block entity (chests...). */
    private static boolean canRip(ServerLevel server, BlockPos pos) {
        BlockState state = server.getBlockState(pos);
        return !state.isAir() && state.getFluidState().isEmpty() && !state.hasBlockEntity() && state.getDestroySpeed(server, pos) >= 0;
    }

    /** Flings a falling block outwards; it lands and is placed back where it ends up. */
    private void fling(FallingBlockEntity block, Vec3 from, double power) {
        Vec3 out = block.position().subtract(from);
        out = out.lengthSqr() < 0.01 ? new Vec3(random.nextGaussian(), 0, random.nextGaussian()) : out;
        out = new Vec3(out.x, 0, out.z).normalize();
        block.setNoGravity(false);
        block.setDeltaMovement(out.scale(power * (0.6 + random.nextDouble())).add(0, 0.5 + random.nextDouble() * 0.9, 0));
        block.needsSync = true;
    }

    /** Crater carved under the explosion, with a scorched rim. */
    private void carveCrater(ServerLevel server) {
        int cx = Mth.floor(getX());
        int cz = Mth.floor(getZ());
        int groundY = server.getHeight(Heightmap.Types.WORLD_SURFACE, cx, cz) - 1;
        if (getY() - groundY > 24) return; // too high above the ground: no crater
        Vec3 blast = new Vec3(cx + 0.5, groundY + 2, cz + 0.5);
        int r = CRATER_RADIUS;
        BlockState[] scorch = {Blocks.BLACKSTONE.defaultBlockState(), Blocks.BLACKSTONE.defaultBlockState(),
                Blocks.CRYING_OBSIDIAN.defaultBlockState(), Blocks.OBSIDIAN.defaultBlockState(), Blocks.MAGMA_BLOCK.defaultBlockState()};

        for (int dx = -r - 3; dx <= r + 3; dx++) {
            for (int dz = -r - 3; dz <= r + 3; dz++) {
                double h = Math.sqrt(dx * dx + dz * dz);
                int x = cx + dx;
                int z = cz + dz;
                if (h <= r) {
                    // Bowl shape: deeper in the centre.
                    int depth = (int) Math.round(Math.sqrt(Math.max(0, r * r - h * h)) / 1.6);
                    for (int y = groundY + 3; y >= groundY - depth; y--) {
                        BlockPos pos = new BlockPos(x, y, z);
                        if (!canRip(server, pos)) continue;
                        if (random.nextFloat() < 0.22F) {
                            FallingBlockEntity block = FallingBlockEntity.fall(server, pos, server.getBlockState(pos));
                            fling(block, blast, 1.4);
                        } else {
                            server.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                        }
                    }
                }
                // Scorching: crater floor and outer rim.
                double scorchChance = h <= r ? 0.55 : (1 - (h - r) / 3.0) * 0.6;
                if (scorchChance > 0 && random.nextFloat() < scorchChance) {
                    BlockPos top = new BlockPos(x, server.getHeight(Heightmap.Types.WORLD_SURFACE, x, z) - 1, z);
                    if (canRip(server, top)) server.setBlock(top, scorch[random.nextInt(scorch.length)], 3);
                }
            }
        }
        server.sendParticles(ParticleTypes.EXPLOSION_EMITTER, true, true, blast.x, blast.y, blast.z, 3, 2, 0.5, 2, 0);
        server.sendParticles(ParticleTypes.LARGE_SMOKE, true, true, blast.x, blast.y, blast.z, 200, r * 0.6, 1, r * 0.6, 0.15);
        server.sendParticles(ParticleTypes.REVERSE_PORTAL, true, true, blast.x, blast.y, blast.z, 300, r * 0.6, 0.5, r * 0.6, 0.6);
    }

    private void explode(ServerLevel server) {
        Vec3 c = position();

        // Swallowed blocks (and ones still in flight) are flung in every direction;
        // everything else that was swallowed is destroyed.
        for (UUID id : captured.keySet()) {
            Entity e = server.getEntity(id);
            if (e instanceof FallingBlockEntity block) fling(block, c, 2.2);
            else if (e != null) destroy(server, e);
        }
        for (Entity e : server.getEntities(this, new AABB(c, c).inflate(pullRange()), this::canPull)) {
            if (e instanceof FallingBlockEntity block) fling(block, c, 1.6);
            else if (e.distanceToSqr(c) < 100) destroy(server, e);
        }
        captured.clear();
        if (CRATER) carveCrater(server);

        // Shockwave pushes players away without hurting them; nearby players also get the advancement.
        AdvancementHolder advancement = server.getServer().getAdvancements().get(BlahajMod.id("what_happened"));
        for (ServerPlayer player : server.players()) {
            if (advancement != null && player.distanceToSqr(c) < 96 * 96) {
                player.getAdvancements().award(advancement, "survived");
            }
            Vec3 away = player.position().subtract(c);
            double d = away.length();
            if (d < 32 && d > 0.01) {
                player.push(away.normalize().scale((1 - d / 32) * 2.4).add(0, 0.6 * (1 - d / 32), 0));
            }
        }

        server.playSound(null, c.x, c.y, c.z, VoidContent.EXPLODE, SoundSource.HOSTILE, 10.0F, 1.0F);
        server.playSound(null, c.x, c.y, c.z, SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 8.0F, 0.5F);
        server.playSound(null, c.x, c.y, c.z, SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.HOSTILE, 8.0F, 0.6F);

        for (int i = 0; i < 8; i++) {
            server.sendParticles(ParticleTypes.EXPLOSION_EMITTER, true, true,
                    c.x + (random.nextDouble() - 0.5) * 6, c.y + (random.nextDouble() - 0.5) * 6, c.z + (random.nextDouble() - 0.5) * 6, 1, 0, 0, 0, 0);
        }
        for (int i = 0; i < 32; i++) {
            double a = i / 32.0 * Math.PI * 2;
            server.sendParticles(ParticleTypes.SONIC_BOOM, true, true, c.x + Math.cos(a) * 5, c.y, c.z + Math.sin(a) * 5, 1, 0, 0, 0, 0);
        }
        server.sendParticles(ParticleTypes.END_ROD, true, true, c.x, c.y, c.z, 400, 0.3, 0.3, 0.3, 1.4);
        server.sendParticles(ParticleTypes.REVERSE_PORTAL, true, true, c.x, c.y, c.z, 600, 0.5, 0.5, 0.5, 2.5);
        server.sendParticles(ParticleTypes.SQUID_INK, true, true, c.x, c.y, c.z, 200, 1.5, 1.5, 1.5, 0.4);
        server.sendParticles(ParticleTypes.LARGE_SMOKE, true, true, c.x, c.y, c.z, 150, 2, 2, 2, 0.3);

        server.broadcastEntityEvent(this, EXPLODE_EVENT);
        discard();
    }

    private void destroy(ServerLevel server, Entity e) {
        if (e instanceof LivingEntity living) {
            living.hurtServer(server, server.damageSources().explosion(this, this), Float.MAX_VALUE);
            if (living.isAlive()) living.kill(server);
        } else {
            e.discard();
        }
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == EXPLODE_EVENT) {
            VoidClientHooks.explosion.accept(this);
        } else {
            super.handleEntityEvent(id);
        }
    }

    /** Local particles: matter spiralling in, sparks in the disc. */
    private void clientParticles() {
        float r = getRadius();
        Level level = level();
        int count = 4 + (int) (r * 6);
        for (int i = 0; i < count; i++) {
            // PORTAL particles spawn at (pos + velocity) and travel to pos, which gives the suction effect.
            double dist = r * 2 + random.nextDouble() * r * 4 + 2;
            double a = random.nextDouble() * Math.PI * 2;
            double y = (random.nextDouble() - 0.5) * dist * 0.5;
            level.addParticle(ParticleTypes.PORTAL, getX(), getY(), getZ(), Math.cos(a) * dist, y, Math.sin(a) * dist);
        }
        for (int i = 0; i < 2 + r; i++) {
            double a = random.nextDouble() * Math.PI * 2;
            double d = r * (1.2 + random.nextDouble() * 1.6);
            level.addParticle(ParticleTypes.END_ROD, getX() + Math.cos(a) * d, getY() + (random.nextDouble() - 0.5) * 0.3, getZ() + Math.sin(a) * d,
                    -Math.sin(a) * 0.15, 0, Math.cos(a) * 0.15);
        }
        if (random.nextFloat() < 0.5F) {
            level.addParticle(ParticleTypes.SQUID_INK, getX() + (random.nextDouble() - 0.5) * r * 2, getY() + (random.nextDouble() - 0.5) * r * 2,
                    getZ() + (random.nextDouble() - 0.5) * r * 2, 0, 0, 0);
        }
        if (isCollapsing()) {
            for (int i = 0; i < 6; i++) {
                double a = random.nextDouble() * Math.PI * 2;
                level.addParticle(ParticleTypes.ELECTRIC_SPARK, getX() + Math.cos(a) * r * 1.5, getY() + (random.nextDouble() - 0.5) * r, getZ() + Math.sin(a) * r * 1.5, 0, 0, 0);
            }
        }
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 256 * 256;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        return false;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {}

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {}

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>("spin", test -> test.setAndContinue(RawAnimation.begin().thenLoop("spin"))));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return geoCache;
    }
}

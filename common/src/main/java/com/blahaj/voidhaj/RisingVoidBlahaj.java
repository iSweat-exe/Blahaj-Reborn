package com.blahaj.voidhaj;

import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.util.GeckoLibUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** The Blåhaj that rises ~10 blocks, shakes, then opens the black hole. */
public class RisingVoidBlahaj extends Entity implements GeoEntity {
    public static final int RISE_TICKS = 45;
    public static final int HOVER_TICKS = 20;
    public static final double HEIGHT = 10.0;

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private double startY;

    public RisingVoidBlahaj(EntityType<? extends RisingVoidBlahaj> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    public void start(double x, double y, double z, float yaw) {
        this.setPos(x, y, z);
        this.setYRot(yaw);
        this.startY = y;
    }

    /** Rise curve: accelerates then eases out (smoothstep). */
    public static double riseCurve(double t) {
        t = Mth.clamp(t, 0, 1);
        return t * t * (3 - 2 * t);
    }

    public int getAge() {
        return this.tickCount;
    }

    @Override
    public void tick() {
        super.tick();
        int age = this.tickCount;

        if (level().isClientSide()) {
            for (int i = 0; i < 4; i++) {
                double a = random.nextDouble() * Math.PI * 2;
                level().addParticle(ParticleTypes.REVERSE_PORTAL, getX() + Math.cos(a) * 0.4, getY() + 0.2, getZ() + Math.sin(a) * 0.4, 0, -0.05, 0);
            }
            level().addParticle(ParticleTypes.WITCH, getX(), getY() + 0.2, getZ(), 0, 0, 0);
            if (age > RISE_TICKS) {
                for (int i = 0; i < 3; i++) {
                    level().addParticle(ParticleTypes.END_ROD, getX() + (random.nextDouble() - 0.5) * 3, getY() + random.nextDouble() * 2 - 0.5,
                            getZ() + (random.nextDouble() - 0.5) * 3, 0, 0, 0);
                }
            }
            VoidClientHooks.risingTick.accept(this);
            return;
        }

        ServerLevel server = (ServerLevel) level();
        if (age == 1) {
            server.playSound(null, getX(), getY(), getZ(), VoidContent.RISE, SoundSource.HOSTILE, 3.0F, 1.0F);
            server.playSound(null, getX(), getY(), getZ(), SoundEvents.BEACON_ACTIVATE, SoundSource.HOSTILE, 2.0F, 0.5F);
        }
        if (age <= RISE_TICKS) {
            setPos(getX(), startY + HEIGHT * riseCurve(age / (double) RISE_TICKS), getZ());
        }
        if (age == RISE_TICKS + HOVER_TICKS) {
            BlackHoleEntity hole = new BlackHoleEntity(VoidContent.BLACK_HOLE, server);
            hole.setPos(getX(), getY() + 0.4, getZ());
            server.addFreshEntity(hole);
            server.playSound(null, getX(), getY(), getZ(), VoidContent.OPEN, SoundSource.HOSTILE, 6.0F, 1.0F);
            server.playSound(null, getX(), getY(), getZ(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.HOSTILE, 4.0F, 0.6F);
            server.playSound(null, getX(), getY(), getZ(), SoundEvents.END_PORTAL_SPAWN, SoundSource.HOSTILE, 4.0F, 0.7F);
            server.sendParticles(ParticleTypes.SONIC_BOOM, getX(), getY() + 0.4, getZ(), 1, 0, 0, 0, 0);
            server.sendParticles(ParticleTypes.REVERSE_PORTAL, getX(), getY() + 0.4, getZ(), 250, 0.2, 0.2, 0.2, 1.5);
            discard();
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {}

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
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {}

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return geoCache;
    }
}

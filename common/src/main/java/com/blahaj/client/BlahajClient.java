package com.blahaj.client;

import com.blahaj.BlahajContent;
import com.blahaj.BlahajMod;
import com.blahaj.voidhaj.VoidClientHooks;
import com.blahaj.voidhaj.VoidContent;
import com.geckolib.model.DefaultedBlockGeoModel;
import com.geckolib.renderer.GeoBlockRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

/** Client setup shared by the three loaders. */
public final class BlahajClient {
    /** Id of the HUD layer used by the void effects. */
    public static final Identifier HUD_LAYER = BlahajMod.id("void_fx");

    public interface RendererRegistrar {
        <T extends BlockEntity> void blockEntity(BlockEntityType<? extends T> type, BlockEntityRendererProvider<T, ?> provider);

        <T extends Entity> void entity(EntityType<? extends T> type, EntityRendererProvider<T> provider);
    }

    /** Connects the client effects (screen, sounds) to the void entities. */
    public static void init() {
        VoidClientHooks.risingTick = VoidClientFx::onRisingTick;
        VoidClientHooks.blackHoleTick = VoidClientFx::onBlackHoleTick;
        VoidClientHooks.explosion = VoidClientFx::onExplosion;
    }

    public static void registerRenderers(RendererRegistrar r) {
        r.blockEntity(BlahajContent.BLOCK_ENTITY, ctx -> new GeoBlockRenderer<>(ctx, BlahajContent.BLOCK_ENTITY));
        r.blockEntity(VoidContent.BLOCK_ENTITY, ctx -> new GeoBlockRenderer<>(ctx, new DefaultedBlockGeoModel<>(BlahajMod.id("blahaj"))));
        r.entity(VoidContent.RISING_BLAHAJ, RisingBlahajRenderer::new);
        r.entity(VoidContent.BLACK_HOLE, BlackHoleRenderer::new);
    }

    private BlahajClient() {}
}

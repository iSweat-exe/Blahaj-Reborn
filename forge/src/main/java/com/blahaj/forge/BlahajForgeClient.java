package com.blahaj.forge;

import com.blahaj.client.BlahajClient;
import com.blahaj.client.VoidClientFx;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.client.event.AddGuiOverlayLayersEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.event.TickEvent;

/** Forge client side (only loaded on the client). */
final class BlahajForgeClient {
    private BlahajForgeClient() {}

    static void init() {
        BlahajClient.init();

        EntityRenderersEvent.RegisterRenderers.BUS.addListener(event -> BlahajClient.registerRenderers(new BlahajClient.RendererRegistrar() {
            @Override
            @SuppressWarnings({"unchecked", "rawtypes"})
            public <T extends BlockEntity> void blockEntity(BlockEntityType<? extends T> type, BlockEntityRendererProvider<T, ?> provider) {
                event.registerBlockEntityRenderer((BlockEntityType) type, (BlockEntityRendererProvider) provider);
            }

            @Override
            public <T extends Entity> void entity(EntityType<? extends T> type, EntityRendererProvider<T> provider) {
                event.registerEntityRenderer(type, provider);
            }
        }));
        AddGuiOverlayLayersEvent.BUS.addListener(event -> event.getLayeredDraw().add(BlahajClient.HUD_LAYER, VoidClientFx::renderHud));
        TickEvent.ClientTickEvent.Post.BUS.addListener(event -> VoidClientFx.tick(Minecraft.getInstance()));
    }
}

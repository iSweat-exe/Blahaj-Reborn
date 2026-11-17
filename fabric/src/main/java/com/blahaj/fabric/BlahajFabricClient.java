package com.blahaj.fabric;

import com.blahaj.client.BlahajClient;
import com.blahaj.client.VoidClientFx;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class BlahajFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BlahajClient.init();
        BlahajClient.registerRenderers(new BlahajClient.RendererRegistrar() {
            @Override
            public <T extends BlockEntity> void blockEntity(BlockEntityType<? extends T> type, BlockEntityRendererProvider<T, ?> provider) {
                BlockEntityRenderers.register(type, provider);
            }

            @Override
            public <T extends Entity> void entity(EntityType<? extends T> type, EntityRendererProvider<T> provider) {
                EntityRenderers.register(type, provider);
            }
        });
        ClientTickEvents.END_CLIENT_TICK.register(VoidClientFx::tick);
        HudElementRegistry.addLast(BlahajClient.HUD_LAYER, VoidClientFx::renderHud);
    }
}

package com.blahaj.neoforge;

import com.blahaj.BlahajMod;
import com.blahaj.client.BlahajClient;
import com.blahaj.client.VoidClientFx;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = BlahajMod.MOD_ID, dist = Dist.CLIENT)
public final class BlahajNeoForgeClient {
    public BlahajNeoForgeClient(IEventBus modBus) {
        BlahajClient.init();

        modBus.addListener(EntityRenderersEvent.RegisterRenderers.class, event -> BlahajClient.registerRenderers(new BlahajClient.RendererRegistrar() {
            @Override
            public <T extends BlockEntity> void blockEntity(BlockEntityType<? extends T> type, BlockEntityRendererProvider<T, ?> provider) {
                event.registerBlockEntityRenderer(type, provider);
            }

            @Override
            public <T extends Entity> void entity(EntityType<? extends T> type, EntityRendererProvider<T> provider) {
                event.registerEntityRenderer(type, provider);
            }
        }));
        modBus.addListener(RegisterGuiLayersEvent.class, event -> event.registerAboveAll(BlahajClient.HUD_LAYER, VoidClientFx::renderHud));
        NeoForge.EVENT_BUS.addListener(ClientTickEvent.Post.class, event -> VoidClientFx.tick(Minecraft.getInstance()));
    }
}

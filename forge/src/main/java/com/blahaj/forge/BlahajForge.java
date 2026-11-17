package com.blahaj.forge;

import com.blahaj.BlahajMod;
import com.blahaj.BlahajRegistry;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.RegisterEvent;

@Mod(BlahajMod.MOD_ID)
public final class BlahajForge {
    public BlahajForge(FMLJavaModLoadingContext context) {
        // RegisterEvent fires once per registry; only register what belongs to it.
        RegisterEvent.getBus(context.getModBusGroup()).addListener(event -> BlahajRegistry.registerAll(new BlahajRegistry.Registrar() {
            @Override
            @SuppressWarnings("unchecked")
            public <T> void register(net.minecraft.resources.ResourceKey<? extends net.minecraft.core.Registry<T>> key,
                                     net.minecraft.resources.Identifier id, java.util.function.Supplier<? extends T> value) {
                if (event.getRegistryKey().equals(key)) {
                    event.register(key, id, (java.util.function.Supplier<T>) value);
                }
            }
        }));

        if (FMLEnvironment.dist == Dist.CLIENT) {
            BlahajForgeClient.init();
        }
    }
}

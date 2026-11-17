package com.blahaj.fabric;

import com.blahaj.BlahajRegistry;
import net.fabricmc.api.ModInitializer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

@SuppressWarnings("unchecked")
public class BlahajFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        // Registries are open during initialization on Fabric, so register directly.
        BlahajRegistry.registerAll(new BlahajRegistry.Registrar() {
            @Override
            public <T> void register(net.minecraft.resources.ResourceKey<? extends Registry<T>> key, net.minecraft.resources.Identifier id, java.util.function.Supplier<? extends T> value) {
                Registry<T> registry = (Registry<T>) BuiltInRegistries.REGISTRY.getValue(key.identifier());
                Registry.register(registry, id, value.get());
            }
        });
    }
}

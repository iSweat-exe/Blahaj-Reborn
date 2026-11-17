package com.blahaj.neoforge;

import com.blahaj.BlahajMod;
import com.blahaj.BlahajRegistry;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.RegisterEvent;

@Mod(BlahajMod.MOD_ID)
public final class BlahajNeoForge {
    public BlahajNeoForge(IEventBus modBus) {
        // RegisterEvent fires once per registry; only register what belongs to it.
        modBus.addListener(RegisterEvent.class, event -> BlahajRegistry.registerAll(new BlahajRegistry.Registrar() {
            @Override
            @SuppressWarnings("unchecked")
            public <T> void register(net.minecraft.resources.ResourceKey<? extends net.minecraft.core.Registry<T>> key,
                                     net.minecraft.resources.Identifier id, java.util.function.Supplier<? extends T> value) {
                if (event.getRegistryKey().equals(key)) {
                    event.register(key, id, (java.util.function.Supplier<T>) value);
                }
            }
        }));
    }
}

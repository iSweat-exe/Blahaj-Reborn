package com.blahaj;

import com.blahaj.voidhaj.VoidContent;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;

import java.util.function.Supplier;

/**
 * Single list of everything the mod registers. Each loader provides a {@link Registrar}
 * (Fabric: direct registration; NeoForge/Forge: during RegisterEvent).
 * Values are passed as Suppliers so they are only resolved when the loader registers them.
 */
public final class BlahajRegistry {
    public interface Registrar {
        <T> void register(ResourceKey<? extends Registry<T>> registry, Identifier id, Supplier<? extends T> value);
    }

    public static void registerAll(Registrar r) {
        r.register(Registries.SOUND_EVENT, BlahajMod.id("squish"), () -> BlahajContent.SQUISH_SOUND);
        for (String name : new String[]{"void.rise", "void.open", "void.loop", "void.glitch", "void.collapse", "void.explode", "void.ring"}) {
            r.register(Registries.SOUND_EVENT, BlahajMod.id(name), () -> soundByName(name));
        }

        r.register(Registries.BLOCK, BlahajContent.ID, () -> BlahajContent.BLOCK);
        r.register(Registries.BLOCK, VoidContent.ID, () -> VoidContent.BLOCK);

        r.register(Registries.BLOCK_ENTITY_TYPE, BlahajContent.ID, () -> BlahajContent.BLOCK_ENTITY);
        r.register(Registries.BLOCK_ENTITY_TYPE, VoidContent.ID, () -> VoidContent.BLOCK_ENTITY);

        r.register(Registries.ITEM, BlahajContent.ID, () -> BlahajContent.ITEM);
        r.register(Registries.ITEM, VoidContent.ID, () -> VoidContent.ITEM);

        r.register(Registries.ENTITY_TYPE, VoidContent.RISING_ID, () -> VoidContent.RISING_BLAHAJ);
        r.register(Registries.ENTITY_TYPE, VoidContent.BLACK_HOLE_ID, () -> VoidContent.BLACK_HOLE);

        r.register(Registries.CREATIVE_MODE_TAB, BlahajContent.TAB_ID, () -> BlahajContent.TAB);
    }

    private static SoundEvent soundByName(String name) {
        Identifier id = BlahajMod.id(name);
        for (SoundEvent sound : VoidContent.SOUNDS) {
            if (sound.location().equals(id)) return sound;
        }
        throw new IllegalArgumentException("Unknown sound " + id);
    }

    private BlahajRegistry() {}
}

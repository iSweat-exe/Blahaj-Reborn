package com.blahaj.voidhaj;

import java.util.function.Consumer;

/**
 * Hooks into the client effects (screen, looping sounds...).
 * Client code replaces them at startup; on a dedicated server they do nothing.
 */
public final class VoidClientHooks {
    public static Consumer<RisingVoidBlahaj> risingTick = e -> {};
    public static Consumer<BlackHoleEntity> blackHoleTick = e -> {};
    public static Consumer<BlackHoleEntity> explosion = e -> {};

    private VoidClientHooks() {}
}

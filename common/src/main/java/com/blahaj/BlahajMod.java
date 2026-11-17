package com.blahaj;

import net.minecraft.resources.Identifier;

public final class BlahajMod {
    public static final String MOD_ID = "blahaj";

    private BlahajMod() {}

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}

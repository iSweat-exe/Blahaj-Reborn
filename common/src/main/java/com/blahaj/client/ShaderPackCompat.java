package com.blahaj.client;

import java.lang.reflect.Method;

/**
 * Detects whether an Iris shader pack is active (via reflection, so Iris stays optional).
 * Packs render their own sky, so the darkening is applied on screen instead.
 */
public final class ShaderPackCompat {
    private static Object irisApi;
    private static Method inUse;
    private static boolean resolved;

    private ShaderPackCompat() {}

    public static boolean isShaderPackActive() {
        if (!resolved) {
            resolved = true;
            try {
                Class<?> api = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
                irisApi = api.getMethod("getInstance").invoke(null);
                inUse = api.getMethod("isShaderPackInUse");
            } catch (ReflectiveOperationException | LinkageError e) {
                irisApi = null;  // Iris not installed
            }
        }
        if (irisApi == null) return false;
        try {
            return (Boolean) inUse.invoke(irisApi);
        } catch (ReflectiveOperationException e) {
            return false;
        }
    }
}

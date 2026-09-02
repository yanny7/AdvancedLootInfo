package com.yanny.alicompat.platform;

import org.jetbrains.annotations.NotNull;

import java.util.ServiceLoader;

public class Services {
    private static volatile ICompatPlatform INSTANCE;

    @NotNull
    public static ICompatPlatform getPlatform() {
        if (INSTANCE == null) {
            synchronized (Services.class) {
                if (INSTANCE == null) {
                    INSTANCE = load(ICompatPlatform.class);
                }
            }
        }
        return INSTANCE;
    }

    @NotNull
    public static <T> T load(Class<T> clazz) {
        return ServiceLoader.load(clazz, clazz.getClassLoader())
                .findFirst()
                .orElseThrow(() -> new NullPointerException("Failed to load service for " + clazz.getName()));
    }
}

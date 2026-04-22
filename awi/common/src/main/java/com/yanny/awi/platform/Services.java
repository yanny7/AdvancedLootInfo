package com.yanny.awi.platform;

import com.yanny.awi.platform.services.IPlatformHelper;
import org.jetbrains.annotations.NotNull;

import java.util.ServiceLoader;

public class Services {
    private static volatile IPlatformHelper INSTANCE;

    public static IPlatformHelper getPlatform() {
        if (INSTANCE == null) {
            synchronized (Services.class) {
                if (INSTANCE == null) {
                    INSTANCE = load(IPlatformHelper.class);
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
package com.yanny.ali.platform;

import com.yanny.ali.platform.services.IPlatformHelper;

import java.util.ServiceLoader;

public class Services {
    private static volatile IPlatformHelper INSTANCE = load(IPlatformHelper.class);

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

    public static <T> T load(Class<T> clazz) {
        return ServiceLoader.load(clazz, clazz.getClassLoader())
                .findFirst()
                .orElseThrow(() -> new NullPointerException("Failed to load service for " + clazz.getName()));
    }
}
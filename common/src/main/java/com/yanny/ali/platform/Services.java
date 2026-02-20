package com.yanny.ali.platform;

import com.yanny.ali.platform.services.IClientPlatformHelper;
import com.yanny.ali.platform.services.IPlatformHelper;

import java.util.ServiceLoader;

public class Services {
    private static volatile IPlatformHelper INSTANCE = load(IPlatformHelper.class);
    private static volatile IClientPlatformHelper CLIENT_INSTANCE;

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

    public static IClientPlatformHelper getClientPlatform() {
        if (CLIENT_INSTANCE == null) {
            synchronized (Services.class) {
                if (CLIENT_INSTANCE == null) {
                    CLIENT_INSTANCE = load(IClientPlatformHelper.class);
                }
            }
        }
        return CLIENT_INSTANCE;
    }

    public static <T> T load(Class<T> clazz) {
        return ServiceLoader.load(clazz, clazz.getClassLoader())
                .findFirst()
                .orElseThrow(() -> new NullPointerException("Failed to load service for " + clazz.getName()));
    }
}
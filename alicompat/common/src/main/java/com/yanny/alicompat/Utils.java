package com.yanny.alicompat;

import org.jetbrains.annotations.NotNull;

public class Utils {
    public static final String MOD_ID = "alicompat";

    @NotNull
    public static String langKey(@NotNull String modId, @NotNull String group, @NotNull String key) {
        return MOD_ID + "." + modId + "." + group + "." + key;
    }
}

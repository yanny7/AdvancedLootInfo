package com.yanny.awi;

import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public class Utils {
    public static final String MOD_ID = "awi";
    public static final String COMMON_CONFIG_NAME = "awi_common.json";

    @NotNull
    public static Identifier modLoc(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}

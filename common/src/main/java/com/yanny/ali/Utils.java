package com.yanny.ali;

import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public class Utils {
    public static final String MOD_ID = "ali";
    public static final String COMMON_CONFIG_NAME = "ali_common.json";

    @NotNull
    public static Identifier modLoc(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}

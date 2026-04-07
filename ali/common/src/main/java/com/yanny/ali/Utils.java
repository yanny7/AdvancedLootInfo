package com.yanny.ali;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class Utils {
    public static final String MOD_ID = "ali";
    public static final String COMMON_CONFIG_NAME = "ali_common.json";

    @NotNull
    public static ResourceLocation modLoc(String path) {
        return  new ResourceLocation(MOD_ID, path);
    }
}

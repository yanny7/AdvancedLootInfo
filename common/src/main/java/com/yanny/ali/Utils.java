package com.yanny.ali;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class Utils {
    public static final String MOD_ID = "ali";

    @NotNull
    public static ResourceLocation modLoc(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}

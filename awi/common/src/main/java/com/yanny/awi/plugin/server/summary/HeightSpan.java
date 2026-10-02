package com.yanny.awi.plugin.server.summary;

import com.yanny.aci.api.NumberExpr;
import net.minecraft.world.level.levelgen.Heightmap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record HeightSpan(@Nullable NumberExpr height, @Nullable Heightmap.Types heightmap) {
    @NotNull
    public static HeightSpan of(NumberExpr height) {
        return new HeightSpan(height, null);
    }

    @NotNull
    public static HeightSpan relativeTo(Heightmap.Types heightmap) {
        return new HeightSpan(null, heightmap);
    }
}

package com.yanny.awi.plugin.server.summary;

import com.yanny.aci.api.NumberExpr;
import com.yanny.aci.tooltip.TooltipNode;
import net.minecraft.world.level.levelgen.Heightmap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record HeightSpan(@Nullable NumberExpr height, List<TooltipNode> conditions, @Nullable Heightmap.Types heightmap) {
    public HeightSpan {
        conditions = List.copyOf(conditions);
    }

    @NotNull
    public static HeightSpan of(NumberExpr height, List<TooltipNode> conditions) {
        return new HeightSpan(height, conditions, null);
    }

    @NotNull
    public static HeightSpan relativeTo(Heightmap.Types heightmap) {
        return new HeightSpan(null, List.of(), heightmap);
    }
}

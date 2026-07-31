package com.yanny.awi.plugin.server.summary;

import net.minecraft.world.level.levelgen.VerticalAnchor;

public record ColumnContext(int minY, int height) {
    /** Mirrors vanilla {@code VerticalAnchor.resolveY(WorldGenerationContext)} without needing a world. */
    public int resolveY(VerticalAnchor anchor) {
        if (anchor instanceof VerticalAnchor.Absolute absolute) {
            return absolute.y();
        } else if (anchor instanceof VerticalAnchor.AboveBottom aboveBottom) {
            return minY + aboveBottom.offset();
        } else if (anchor instanceof VerticalAnchor.BelowTop belowTop) {
            return minY + height - 1 - belowTop.offset();
        } else {
            return minY;
        }
    }
}

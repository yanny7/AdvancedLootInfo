package com.yanny.awi.plugin.server.summary;

import net.minecraft.world.level.levelgen.VerticalAnchor;

public record ColumnContext(int minY, int height) {
    /** Mirrors vanilla {@code VerticalAnchor.resolveY(WorldGenerationContext)} without needing a world. */
    public int resolveY(VerticalAnchor anchor) {
        return switch (anchor) {
            case VerticalAnchor.Absolute(int y) -> y;
            case VerticalAnchor.AboveBottom(int offset) -> minY + offset;
            case VerticalAnchor.BelowTop(int offset) -> minY + height - 1 - offset;
            default -> minY;
        };
    }
}

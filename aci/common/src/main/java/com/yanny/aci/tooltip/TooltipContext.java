package com.yanny.aci.tooltip;

import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TooltipContext {
    private static final ThreadLocal<Identifier> CURRENT_LOOT_TABLE = new ThreadLocal<>();
    private static final ThreadLocal<TooltipNodePalette> CURRENT_PALETTE = new ThreadLocal<>();

    public static void set(@Nullable Identifier lootTable) {
        CURRENT_LOOT_TABLE.set(lootTable);
    }

    public static Identifier get() {
        return CURRENT_LOOT_TABLE.get();
    }

    public static void clear() {
        CURRENT_LOOT_TABLE.remove();
    }

    public static void setPalette(TooltipNodePalette palette) {
        CURRENT_PALETTE.set(palette);
    }

    @NotNull
    public static TooltipNodePalette getPalette() {
        TooltipNodePalette palette = CURRENT_PALETTE.get();

        if (palette == null) {
            throw new IllegalStateException("No tooltip palette bound to this thread. Tooltip building must be bracketed by "
                    + "TooltipContext.setPalette(serverRegistry.getTooltipCache()) and TooltipContext.clearPalette().");
        }

        return palette;
    }

    public static void clearPalette() {
        CURRENT_PALETTE.remove();
    }
}

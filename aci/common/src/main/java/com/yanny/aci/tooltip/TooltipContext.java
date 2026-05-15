package com.yanny.aci.tooltip;

import net.minecraft.resources.ResourceLocation;

public class TooltipContext {
    private static final ThreadLocal<ResourceLocation> CURRENT_LOOT_TABLE = new ThreadLocal<>();

    public static void set(ResourceLocation lootTable) {
        CURRENT_LOOT_TABLE.set(lootTable);
    }

    public static ResourceLocation get() {
        return  CURRENT_LOOT_TABLE.get();
    }

    public static void clear() {
        CURRENT_LOOT_TABLE.remove();
    }
}

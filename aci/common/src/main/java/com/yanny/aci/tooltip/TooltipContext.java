package com.yanny.aci.tooltip;

import net.minecraft.resources.Identifier;

public class TooltipContext {
    private static final ThreadLocal<Identifier> CURRENT_LOOT_TABLE = new ThreadLocal<>();

    public static void set(Identifier lootTable) {
        CURRENT_LOOT_TABLE.set(lootTable);
    }

    public static Identifier get() {
        return  CURRENT_LOOT_TABLE.get();
    }

    public static void clear() {
        CURRENT_LOOT_TABLE.remove();
    }
}

package com.yanny.alicompat.compat.apotheosis;

import dev.shadowsoffire.apotheosis.loot.AffixLootEntry;
import dev.shadowsoffire.placebo.dynreg.DynamicHolder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class ApotheosisUtils {
    private static final Identifier GEM = Identifier.fromNamespaceAndPath(ApotheosisLang.MOD_ID, "gem");

    @NotNull
    public static ItemStack gemStack() {
        return new ItemStack(BuiltInRegistries.ITEM.getValue(GEM));
    }

    @NotNull
    public static ItemStack firstEntryStack(Collection<DynamicHolder<AffixLootEntry>> entries) {
        return entries.stream().filter(DynamicHolder::isBound).findFirst().map((h) -> h.get().stack().copy()).orElse(ItemStack.EMPTY);
    }
}

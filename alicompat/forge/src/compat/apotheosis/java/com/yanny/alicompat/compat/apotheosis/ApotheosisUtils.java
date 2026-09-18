package com.yanny.alicompat.compat.apotheosis;

import com.yanny.aci.tooltip.TooltipContext;
import dev.shadowsoffire.apotheosis.adventure.AdventureConfig;
import dev.shadowsoffire.apotheosis.adventure.loot.AffixLootEntry;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ApotheosisUtils {
    private static final ResourceLocation GEM = new ResourceLocation(ApotheosisLang.MOD_ID, "gem");
    private static final ResourceLocation WARDEN_TENDRIL = new ResourceLocation(ApotheosisLang.MOD_ID, "warden_tendril");

    @NotNull
    public static ItemStack gemStack() {
        return new ItemStack(BuiltInRegistries.ITEM.get(GEM));
    }

    @NotNull
    public static ItemStack wardenTendrilStack() {
        return new ItemStack(BuiltInRegistries.ITEM.get(WARDEN_TENDRIL));
    }

    @NotNull
    public static ItemStack firstEntryStack(List<DynamicHolder<AffixLootEntry>> entries) {
        return entries.stream().filter(DynamicHolder::isBound).findFirst().map((h) -> h.get().getStack().copy()).orElse(ItemStack.EMPTY);
    }

    public static float chance(List<AdventureConfig.LootPatternMatcher> rules) {
        ResourceLocation location = TooltipContext.get();

        if (location == null) {
            return 0;
        }

        return rules.stream().filter((r) -> r.matches(location)).findFirst().map(AdventureConfig.LootPatternMatcher::chance).orElse(0.0F);
    }

    public static boolean matches(List<AdventureConfig.LootPatternMatcher> rules, ResourceLocation location) {
        return rules.stream().anyMatch((r) -> r.matches(location));
    }
}

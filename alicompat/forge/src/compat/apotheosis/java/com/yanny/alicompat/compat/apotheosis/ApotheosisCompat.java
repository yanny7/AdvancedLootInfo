package com.yanny.alicompat.compat.apotheosis;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.ILootModifier;
import com.yanny.ali.api.IServerRegistry;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.plugin.glm.IGlobalLootModifierPlugin;
import com.yanny.alicompat.IGlmModCompat;
import com.yanny.alicompat.accessor.GlmAccessorUtils;
import com.yanny.alicompat.accessor.PluginUtils;
import dev.shadowsoffire.apotheosis.adventure.affix.trades.AffixTrade;
import dev.shadowsoffire.apotheosis.adventure.loot.AffixConvertLootModifier;
import dev.shadowsoffire.apotheosis.adventure.loot.AffixHookLootModifier;
import dev.shadowsoffire.apotheosis.adventure.loot.AffixLootModifier;
import dev.shadowsoffire.apotheosis.adventure.loot.AffixLootPoolEntry;
import dev.shadowsoffire.apotheosis.adventure.loot.GemLootModifier;
import dev.shadowsoffire.apotheosis.adventure.loot.GemLootPoolEntry;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.loot.RarityClamp;
import dev.shadowsoffire.apotheosis.adventure.loot.RarityRegistry;
import dev.shadowsoffire.apotheosis.ench.objects.WardenLootModifier;
import dev.shadowsoffire.apotheosis.util.AffixItemIngredient;
import dev.shadowsoffire.apotheosis.util.GemIngredient;
import dev.shadowsoffire.apotheosis.village.wanderer.WandererTrade;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class ApotheosisCompat implements IGlmModCompat {
    @NotNull
    @Override
    public String targetModId() {
        return ApotheosisLang.MOD_ID;
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        PluginUtils.registerEntry(registry, AffixLootPoolEntry.class, AffixLootPoolEntryAccessor.class);
        PluginUtils.registerEntryTooltip(registry, AffixLootPoolEntry.class, AffixLootPoolEntryAccessor.class);
        PluginUtils.registerEntry(registry, GemLootPoolEntry.class, GemLootPoolEntryAccessor.class);
        PluginUtils.registerEntryTooltip(registry, GemLootPoolEntry.class, GemLootPoolEntryAccessor.class);

        PluginUtils.registerIngredientTooltip(registry, AffixItemIngredient.class, AffixItemIngredientAccessor.class);
        PluginUtils.registerIngredientTooltip(registry, GemIngredient.class, GemIngredientAccessor.class);

        registry.registerValueTooltip(DynamicHolder.class, ApotheosisCompat::getDynamicHolderTooltip);
        registry.registerValueTooltip(RarityClamp.Simple.class, ApotheosisCompat::getRarityClampTooltip);
        registry.registerValueTooltip(LootRarity.class, ApotheosisCompat::getLootRarityTooltip);

        PluginUtils.registerItemListing(registry, AffixTrade.class, AffixTradeAccessor.class);
        PluginUtils.registerItemListing(registry, WandererTrade.class, WandererTradeAccessor.class);

        PluginUtils.registerDestination(registry, AffixLootModifier.class, AffixLootModifierAccessor.class);
        PluginUtils.registerDestination(registry, AffixConvertLootModifier.class, AffixConvertLootModifierAccessor.class);
        PluginUtils.registerDestination(registry, GemLootModifier.class, GemLootModifierAccessor.class);
        PluginUtils.registerDestination(registry, WardenLootModifier.class, WardenLootModifierAccessor.class);
    }

    @Override
    public void registerGlobalLootModifier(IGlobalLootModifierPlugin.IRegistry registry) {
        GlmAccessorUtils.registerGlobalLootModifier(registry, AffixLootModifier.class, AffixLootModifierAccessor.class);
        GlmAccessorUtils.registerGlobalLootModifier(registry, AffixConvertLootModifier.class, AffixConvertLootModifierAccessor.class);
        GlmAccessorUtils.registerGlobalLootModifier(registry, GemLootModifier.class, GemLootModifierAccessor.class);
        GlmAccessorUtils.registerGlobalLootModifier(registry, WardenLootModifier.class, WardenLootModifierAccessor.class);

        registry.registerGlobalLootModifier(AffixHookLootModifier.class, ApotheosisCompat::getAffixHookLootModifier);
    }

    @NotNull
    private static Optional<ILootModifier<?>> getAffixHookLootModifier(IServerUtils ignoredUtils, AffixHookLootModifier ignoredModifier) {
        return Optional.empty();
    }

    @NotNull
    private static TooltipBuilder getDynamicHolderTooltip(IServerUtils utils, DynamicHolder<?> holder) {
        return utils.getValueTooltip(utils, holder.getId());
    }

    @NotNull
    private static TooltipBuilder getLootRarityTooltip(IServerUtils utils, LootRarity rarity) {
        return utils.getValueTooltip(utils, RarityRegistry.INSTANCE.getKey(rarity));
    }

    @NotNull
    private static TooltipBuilder getRarityClampTooltip(IServerUtils utils, RarityClamp.Simple rarities) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, rarities.min()).build(ApotheosisLang.Value.MIN_RARITY));
            b.add(utils.getValueTooltip(utils, rarities.max()).build(ApotheosisLang.Value.MAX_RARITY));
        });
    }
}

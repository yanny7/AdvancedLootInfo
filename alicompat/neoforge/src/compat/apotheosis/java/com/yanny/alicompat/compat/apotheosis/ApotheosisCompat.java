package com.yanny.alicompat.compat.apotheosis;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.ILootModifier;
import com.yanny.ali.api.IServerRegistry;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import com.yanny.ali.plugin.common.trades.SubTradesNode;
import com.yanny.ali.plugin.glm.Destination;
import com.yanny.ali.plugin.glm.IGlobalLootModifierPlugin;
import com.yanny.alicompat.IGlmModCompat;
import com.yanny.alicompat.accessor.GlmAccessorUtils;
import com.yanny.alicompat.accessor.PluginUtils;
import dev.shadowsoffire.apotheosis.advancements.predicates.AffixItemPredicate;
import dev.shadowsoffire.apotheosis.advancements.predicates.InvaderPredicate;
import dev.shadowsoffire.apotheosis.advancements.predicates.MonsterPredicate;
import dev.shadowsoffire.apotheosis.advancements.predicates.PurityItemPredicate;
import dev.shadowsoffire.apotheosis.advancements.predicates.RarityItemPredicate;
import dev.shadowsoffire.apotheosis.advancements.predicates.SocketItemPredicate;
import dev.shadowsoffire.apotheosis.affix.trades.AffixTrade;
import dev.shadowsoffire.apotheosis.affix.trades.AutomaticAffixTrade;
import dev.shadowsoffire.apotheosis.affix.trades.TieredTrade;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.apotheosis.loot.RarityRegistry;
import dev.shadowsoffire.apotheosis.loot.conditions.KilledByRealPlayerCondition;
import dev.shadowsoffire.apotheosis.loot.conditions.MatchesBlockCondition;
import dev.shadowsoffire.apotheosis.loot.conditions.WorldTierCondition;
import dev.shadowsoffire.apotheosis.loot.entry.AffixLootPoolEntry;
import dev.shadowsoffire.apotheosis.loot.entry.GemLootPoolEntry;
import dev.shadowsoffire.apotheosis.loot.functions.ReforgeItemFunction;
import dev.shadowsoffire.apotheosis.loot.modifiers.AffixConvertLootModifier;
import dev.shadowsoffire.apotheosis.loot.modifiers.AffixHookLootModifier;
import dev.shadowsoffire.apotheosis.loot.modifiers.AffixLootModifier;
import dev.shadowsoffire.apotheosis.loot.modifiers.GemLootModifier;
import dev.shadowsoffire.apotheosis.socket.gem.Purity;
import dev.shadowsoffire.apotheosis.tiers.WorldTier;
import dev.shadowsoffire.apotheosis.util.AffixItemIngredient;
import dev.shadowsoffire.apotheosis.util.GemIngredient;
import dev.shadowsoffire.apotheosis.util.LootPatternMatcher;
import dev.shadowsoffire.apotheosis.util.SpawnEggIngredient;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import dev.shadowsoffire.placebo.systems.wanderer.WandererTrade;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

        PluginUtils.registerFunctionTooltip(registry, ReforgeItemFunction.class, ReforgeItemFunctionAccessor.class);

        PluginUtils.registerValueTooltip(registry, AffixItemIngredient.class, AffixItemIngredientAccessor.class);
        registry.registerValueTooltip(GemIngredient.class, ApotheosisCompat::getGemIngredientTooltip);
        registry.registerValueTooltip(SpawnEggIngredient.class, ApotheosisCompat::getSpawnEggIngredientTooltip);

        registry.registerValueTooltip(DynamicHolder.class, ApotheosisCompat::getDynamicHolderTooltip);
        registry.registerValueTooltip(LootRarity.class, ApotheosisCompat::getLootRarityTooltip);
        registry.registerValueTooltip(Purity.class, ApotheosisCompat::getPurityTooltip);
        registry.registerValueTooltip(WorldTier.class, ApotheosisCompat::getWorldTierTooltip);

        registry.registerConditionTooltip(KilledByRealPlayerCondition.class, ApotheosisCompat::getKilledByRealPlayerTooltip);
        registry.registerConditionTooltip(MatchesBlockCondition.class, ApotheosisCompat::getMatchesBlockTooltip);
        registry.registerConditionTooltip(WorldTierCondition.class, ApotheosisCompat::getWorldTierConditionTooltip);
        registry.registerConditionTooltip(LootPatternMatcher.class, ApotheosisCompat::getLootPatternMatcherTooltip);

        registry.registerEntitySubPredicateTooltip(MonsterPredicate.CODEC, ApotheosisCompat::getMonsterPredicateTooltip);
        registry.registerEntitySubPredicateTooltip(InvaderPredicate.CODEC, ApotheosisCompat::getInvaderPredicateTooltip);

        registry.registerItemSubPredicateTooltip(AffixItemPredicate.class, ApotheosisCompat::getAffixItemPredicateTooltip);
        registry.registerItemSubPredicateTooltip(PurityItemPredicate.class, ApotheosisCompat::getPurityItemPredicateTooltip);
        registry.registerItemSubPredicateTooltip(RarityItemPredicate.class, ApotheosisCompat::getRarityItemPredicateTooltip);
        registry.registerItemSubPredicateTooltip(SocketItemPredicate.class, ApotheosisCompat::getSocketItemPredicateTooltip);

        PluginUtils.registerItemListing(registry, AffixTrade.class, AffixTradeAccessor.class);
        PluginUtils.registerItemListing(registry, AutomaticAffixTrade.class, AutomaticAffixTradeAccessor.class);
        registry.registerItemListing(TieredTrade.class, ApotheosisCompat::getTieredTradeNode);

        PluginUtils.registerDestination(registry, AffixLootModifier.class, AffixLootModifierAccessor.class);
        PluginUtils.registerDestination(registry, AffixConvertLootModifier.class, AffixConvertLootModifierAccessor.class);
        PluginUtils.registerDestination(registry, GemLootModifier.class, GemLootModifierAccessor.class);
        registry.registerDestination(MatchesBlockCondition.class, ApotheosisCompat::getMatchesBlockDestination);
        registry.registerDestination(LootPatternMatcher.class, ApotheosisCompat::getLootPatternMatcherDestination);
    }

    @Override
    public void registerGlobalLootModifier(IGlobalLootModifierPlugin.IRegistry registry) {
        GlmAccessorUtils.registerGlobalLootModifier(registry, AffixLootModifier.class, AffixLootModifierAccessor.class);
        GlmAccessorUtils.registerGlobalLootModifier(registry, AffixConvertLootModifier.class, AffixConvertLootModifierAccessor.class);
        GlmAccessorUtils.registerGlobalLootModifier(registry, GemLootModifier.class, GemLootModifierAccessor.class);

        registry.registerGlobalLootModifier(AffixHookLootModifier.class, ApotheosisCompat::getAffixHookLootModifier);
    }

    @NotNull
    private static Optional<ILootModifier<?>> getAffixHookLootModifier(IServerUtils ignoredUtils, AffixHookLootModifier ignoredModifier) {
        return Optional.empty();
    }

    @NotNull
    private static TooltipBuilder getGemIngredientTooltip(IServerUtils utils, GemIngredient ingredient) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, ingredient.purity()).build(ApotheosisLang.Value.PURITY));
            b.add(utils.getValueTooltip(utils, ingredient.gems()).build(Lang.Branch.ENTRIES));
        }, ApotheosisLang.Ingredient.GEM);
    }

    @NotNull
    private static TooltipBuilder getSpawnEggIngredientTooltip(IServerUtils ignoredUtils, SpawnEggIngredient ignoredIngredient) {
        return TooltipBuilder.array(TooltipBuilder::showEmpty, ApotheosisLang.Ingredient.SPAWN_EGG);
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
    private static TooltipBuilder getPurityTooltip(IServerUtils utils, Purity purity) {
        return utils.getValueTooltip(utils, purity.getSerializedName());
    }

    @NotNull
    private static TooltipBuilder getWorldTierTooltip(IServerUtils utils, WorldTier tier) {
        return utils.getValueTooltip(utils, tier.getSerializedName());
    }

    @NotNull
    private static TooltipBuilder getKilledByRealPlayerTooltip(IServerUtils ignoredUtils, KilledByRealPlayerCondition ignoredCond) {
        return TooltipBuilder.array(TooltipBuilder::showEmpty, ApotheosisLang.Conditions.KILLED_BY_REAL_PLAYER);
    }

    @NotNull
    private static TooltipBuilder getMatchesBlockTooltip(IServerUtils utils, MatchesBlockCondition cond) {
        return TooltipBuilder.array((b) -> b.add(utils.getValueTooltip(utils, cond.blocks()).build(Lang.Branch.BLOCKS)), ApotheosisLang.Conditions.MATCHES_BLOCK);
    }

    @NotNull
    private static TooltipBuilder getWorldTierConditionTooltip(IServerUtils utils, WorldTierCondition cond) {
        return TooltipBuilder.array((b) -> b.add(utils.getValueTooltip(utils, cond.tiers()).build(ApotheosisLang.Branch.WORLD_TIERS)), ApotheosisLang.Conditions.WORLD_TIER);
    }

    @NotNull
    private static TooltipBuilder getLootPatternMatcherTooltip(IServerUtils utils, LootPatternMatcher cond) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, cond.domain()).build(ApotheosisLang.Value.DOMAIN));
            b.add(utils.getValueTooltip(utils, cond.pathRegex().pattern()).build(Lang.Value.PATTERN));
        }, ApotheosisLang.Conditions.LOOT_TABLE_ID_PATTERN);
    }

    @NotNull
    private static Destination getMatchesBlockDestination(IServerUtils ignoredUtils, MatchesBlockCondition cond) {
        return new Destination.Blocks((block) -> cond.blocks().contains(block.builtInRegistryHolder()), true);
    }

    @NotNull
    private static Destination getLootPatternMatcherDestination(IServerUtils ignoredUtils, LootPatternMatcher cond) {
        return new Destination.Table(cond::matches, true);
    }

    @NotNull
    private static TooltipBuilder getMonsterPredicateTooltip(IServerUtils ignoredUtils, MonsterPredicate ignoredPredicate) {
        return TooltipBuilder.array(TooltipBuilder::showEmpty, ApotheosisLang.EntitySubPredicates.IS_MONSTER);
    }

    @NotNull
    private static TooltipBuilder getInvaderPredicateTooltip(IServerUtils ignoredUtils, InvaderPredicate ignoredPredicate) {
        return TooltipBuilder.array(TooltipBuilder::showEmpty, ApotheosisLang.EntitySubPredicates.IS_INVADER);
    }

    @NotNull
    private static TooltipBuilder getAffixItemPredicateTooltip(IServerUtils ignoredUtils, AffixItemPredicate ignoredPredicate) {
        return TooltipBuilder.array(TooltipBuilder::showEmpty, ApotheosisLang.ItemSubPredicates.AFFIXED_ITEM);
    }

    @NotNull
    private static TooltipBuilder getPurityItemPredicateTooltip(IServerUtils utils, PurityItemPredicate predicate) {
        return TooltipBuilder.array((b) -> b.add(utils.getValueTooltip(utils, predicate.purities()).build(ApotheosisLang.Branch.PURITY)), ApotheosisLang.ItemSubPredicates.ITEM_WITH_PURITY);
    }

    @NotNull
    private static TooltipBuilder getRarityItemPredicateTooltip(IServerUtils utils, RarityItemPredicate predicate) {
        return TooltipBuilder.array((b) -> b.add(utils.getValueTooltip(utils, predicate.rarities()).build(ApotheosisLang.Branch.RARITY)), ApotheosisLang.ItemSubPredicates.ITEM_WITH_RARITY);
    }

    @NotNull
    private static TooltipBuilder getSocketItemPredicateTooltip(IServerUtils ignoredUtils, SocketItemPredicate ignoredPredicate) {
        return TooltipBuilder.array(TooltipBuilder::showEmpty, ApotheosisLang.ItemSubPredicates.SOCKETED_ITEM);
    }

    @NotNull
    private static IDataNode getTieredTradeNode(IServerUtils utils, TieredTrade listing, TooltipNode condition) {
        return new SubTradesNode<>(utils, listing, condition) {
            @Override
            public List<IDataNode> getSubTrades(IServerUtils utils, TieredTrade listing) {
                List<IDataNode> nodes = new ArrayList<>();

                for (Map.Entry<WorldTier, WandererTrade> entry : listing.trades().entrySet()) {
                    TooltipNode cond = utils.getValueTooltip(utils, entry.getKey()).build(ApotheosisLang.Value.WORLD_TIER);

                    nodes.add(utils.getItemListing(utils, entry.getValue(), cond));
                }

                return nodes;
            }
        };
    }
}

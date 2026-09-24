package com.yanny.alicompat.compat.apotheosis;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerRegistry;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import com.yanny.ali.plugin.glm.*;
import com.yanny.alicompat.IGlmModCompat;
import com.yanny.alicompat.accessor.GlmAccessorUtils;
import com.yanny.alicompat.accessor.PluginUtils;
import dev.shadowsoffire.apotheosis.advancements.predicates.*;
import dev.shadowsoffire.apotheosis.affix.trades.AutomaticAffixTrade;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.apotheosis.loot.RarityRegistry;
import dev.shadowsoffire.apotheosis.loot.conditions.KilledByRealPlayerCondition;
import dev.shadowsoffire.apotheosis.loot.conditions.MatchesBlockCondition;
import dev.shadowsoffire.apotheosis.loot.conditions.WorldTierCondition;
import dev.shadowsoffire.apotheosis.loot.entry.AffixLootPoolEntry;
import dev.shadowsoffire.apotheosis.loot.entry.GemLootPoolEntry;
import dev.shadowsoffire.apotheosis.loot.functions.ReforgeItemFunction;
import dev.shadowsoffire.apotheosis.loot.functions.TierGatedTrade;
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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Monster;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
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
        PluginUtils.registerFunctionTooltip(registry, AutomaticAffixTrade.class, AutomaticAffixTradeAccessor.class);
        PluginUtils.registerFunctionTooltip(registry, TierGatedTrade.class, TierGatedTradeAccessor.class);

        PluginUtils.registerItemStackModifier(registry, AutomaticAffixTrade.class, AutomaticAffixTradeAccessor.class);

        PluginUtils.registerValueTooltip(registry, AffixItemIngredient.class, AffixItemIngredientAccessor.class);
        registry.registerValueTooltip(GemIngredient.class, ApotheosisCompat::getGemIngredientTooltip);
        registry.registerValueTooltip(SpawnEggIngredient.class, ApotheosisCompat::getSpawnEggIngredientTooltip);

        registry.registerValueTooltip(LootRarity.class, ApotheosisCompat::getLootRarityTooltip);
        registry.registerValueTooltip(Purity.class, ApotheosisCompat::getPurityTooltip);
        registry.registerValueTooltip(WorldTier.class, ApotheosisCompat::getWorldTierTooltip);

        registry.registerConditionTooltip(KilledByRealPlayerCondition.class, ApotheosisCompat::getKilledByRealPlayerTooltip);
        registry.registerConditionTooltip(MatchesBlockCondition.class, ApotheosisCompat::getMatchesBlockTooltip);
        registry.registerConditionTooltip(WorldTierCondition.class, ApotheosisCompat::getWorldTierConditionTooltip);
        registry.registerConditionTooltip(LootPatternMatcher.class, ApotheosisCompat::getLootPatternMatcherTooltip);

        registry.registerEntitySubPredicateTooltip(MonsterPredicate.CODEC, ApotheosisCompat::getMonsterPredicateTooltip);
        registry.registerEntitySubPredicateTooltip(InvaderPredicate.CODEC, ApotheosisCompat::getInvaderPredicateTooltip);

        registry.registerDataComponentPredicateTooltip(AffixItemPredicate.class, ApotheosisCompat::getAffixItemPredicateTooltip);
        registry.registerDataComponentPredicateTooltip(PurityItemPredicate.class, ApotheosisCompat::getPurityItemPredicateTooltip);
        registry.registerDataComponentPredicateTooltip(RarityItemPredicate.class, ApotheosisCompat::getRarityItemPredicateTooltip);
        registry.registerDataComponentPredicateTooltip(SocketItemPredicate.class, ApotheosisCompat::getSocketItemPredicateTooltip);

        PluginUtils.registerPageResolver(registry, AffixLootModifier.class, AffixLootModifierAccessor.class);
        PluginUtils.registerPageResolver(registry, AffixConvertLootModifier.class, AffixConvertLootModifierAccessor.class);
        PluginUtils.registerPageResolver(registry, GemLootModifier.class, GemLootModifierAccessor.class);
        registry.registerPageResolver(MatchesBlockCondition.class, ApotheosisCompat::testMatchesBlock);

        registry.registerEntitySubPredicateResolver(MonsterPredicate.CODEC, ApotheosisCompat::testMonsterPredicate);
    }

    @Override
    public void registerGlobalLootModifier(IGlobalLootModifierPlugin.IRegistry registry) {
        GlmAccessorUtils.registerGlobalLootModifier(registry, AffixLootModifier.class, AffixLootModifierAccessor.class);
        GlmAccessorUtils.registerGlobalLootModifier(registry, AffixConvertLootModifier.class, AffixConvertLootModifierAccessor.class);
        GlmAccessorUtils.registerGlobalLootModifier(registry, GemLootModifier.class, GemLootModifierAccessor.class);

        registry.registerGlobalLootModifier(AffixHookLootModifier.class, ApotheosisCompat::getAffixHookLootModifier);
    }

    @NotNull
    private static Optional<IPageLootModifier> getAffixHookLootModifier(IServerUtils ignoredUtils, AffixHookLootModifier ignoredModifier) {
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

    @Nullable
    private static Verdict testMatchesBlock(IServerUtils ignoredUtils, MatchesBlockCondition cond, LootPage page) {
        if (page.blocks().isEmpty()) {
            return null;
        }

        return GlobalLootModifierUtils.testBlocks(page, (block) -> cond.blocks().contains(block.builtInRegistryHolder()), true);
    }

    @Nullable
    private static Verdict testMonsterPredicate(IServerUtils ignoredUtils, MonsterPredicate ignoredPredicate, LootPage page) {
        List<Entity> samples = page.samples().get();

        if (samples.isEmpty()) {
            return null;
        }

        long matching = samples.stream().filter(Monster.class::isInstance).count();

        if (matching == 0) {
            return Verdict.NO;
        }

        return Verdict.yes(matching == samples.size());
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
}

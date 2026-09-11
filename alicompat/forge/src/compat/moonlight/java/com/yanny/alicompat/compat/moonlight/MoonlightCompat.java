package com.yanny.alicompat.compat.moonlight;

import com.mojang.datafixers.util.Either;
import com.yanny.aci.api.RangeValue;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerRegistry;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import com.yanny.ali.plugin.common.trades.ItemsToItemsNode;
import com.yanny.ali.plugin.glm.Destination;
import com.yanny.ali.plugin.glm.IGlobalLootModifierPlugin;
import com.yanny.ali.plugin.server.IngredientTooltipUtils;
import com.yanny.ali.plugin.server.TooltipUtils;
import com.yanny.alicompat.IGlmModCompat;
import com.yanny.alicompat.accessor.GlmAccessorUtils;
import com.yanny.alicompat.accessor.PluginUtils;
import net.mehvahdjukaar.moonlight.api.resources.recipe.forge.BlockTypeSwapIngredientImpl;
import net.mehvahdjukaar.moonlight.api.trades.SimpleItemListing;
import net.mehvahdjukaar.moonlight.core.loot.OptionalItemPool;
import net.mehvahdjukaar.moonlight.core.loot.OptionalPropertyCondition;
import net.mehvahdjukaar.moonlight.core.misc.forge.ModLootConditions;
import net.mehvahdjukaar.moonlight.core.misc.forge.ModLootModifiers;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.regex.Pattern;

public class MoonlightCompat implements IGlmModCompat {
    @NotNull
    @Override
    public String targetModId() {
        return MoonlightLang.MOD_ID;
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        PluginUtils.registerEntry(registry, OptionalItemPool.class, OptionalItemPoolAccessor.class);
        PluginUtils.registerEntryTooltip(registry, OptionalItemPool.class, OptionalItemPoolAccessor.class);

        PluginUtils.registerConditionTooltip(registry, OptionalPropertyCondition.class, OptionalPropertyConditionAccessor.class);
        registry.registerConditionTooltip(ModLootConditions.IConditionLootCondition.class, MoonlightCompat::getDataConditionsTooltip);
        registry.registerConditionTooltip(ModLootConditions.PatternMatchCondition.class, MoonlightCompat::getPatternMatchTooltip);

        registry.registerIngredientTooltip(BlockTypeSwapIngredientImpl.class, IngredientTooltipUtils::getIngredientTooltip);

        registry.registerValueTooltip(ICondition.class, MoonlightCompat::getConditionTooltip);
        registry.registerValueTooltip(Pattern.class, MoonlightCompat::getPatternTooltip);

        PluginUtils.registerDestination(registry, OptionalPropertyCondition.class, OptionalPropertyConditionAccessor.class);
        registry.registerDestination(ModLootConditions.PatternMatchCondition.class, MoonlightCompat::getPatternMatchDestination);

        registry.registerItemListing(SimpleItemListing.class, MoonlightCompat::getSimpleItemListingNode);
        PluginUtils.registerItemListing(registry, SpecialListingAccessor.class);
    }

    @Override
    public void registerGlobalLootModifier(IGlobalLootModifierPlugin.IRegistry registry) {
        GlmAccessorUtils.registerGlobalLootModifier(registry, ModLootModifiers.AddItemModifier.class, AddItemModifierAccessor.class);
        GlmAccessorUtils.registerGlobalLootModifier(registry, ModLootModifiers.AddTableModifier.class, AddTableModifierAccessor.class);
        GlmAccessorUtils.registerGlobalLootModifier(registry, ModLootModifiers.ReplaceItemModifier.class, ReplaceItemModifierAccessor.class);
    }

    @NotNull
    private static TooltipBuilder getDataConditionsTooltip(IServerUtils utils, ModLootConditions.IConditionLootCondition cond) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, cond.conditions()));
            b.showEmpty();
        }, MoonlightLang.Conditions.DATA_CONDITIONS);
    }

    @NotNull
    private static TooltipBuilder getPatternMatchTooltip(IServerUtils utils, ModLootConditions.PatternMatchCondition cond) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, cond.patterns()));
            b.showEmpty();
        }, MoonlightLang.Conditions.LOOT_TABLE_ID_PATTERN);
    }

    @NotNull
    private static TooltipBuilder getConditionTooltip(IServerUtils utils, ICondition cond) {
        return TooltipUtils.getJsonTooltip(utils, CraftingHelper.serialize(cond));
    }

    @NotNull
    private static TooltipBuilder getPatternTooltip(IServerUtils ignoredUtils, Pattern pattern) {
        return TooltipBuilder.value(pattern.pattern());
    }

    @NotNull
    private static Destination getPatternMatchDestination(IServerUtils ignoredUtils, ModLootConditions.PatternMatchCondition cond) {
        return new Destination.Table((id) -> matches(cond.patterns(), id.toString()), true);
    }

    private static boolean matches(List<Pattern> patterns, String id) {
        return patterns.stream().anyMatch((p) -> id.equals(p.pattern()) || p.matcher(id).find());
    }

    @NotNull
    private static IDataNode getSimpleItemListingNode(IServerUtils utils, SimpleItemListing listing, TooltipNode condition) {
        return new ItemsToItemsNode(
                utils,
                Either.left(listing.price()),
                new RangeValue(listing.price().getCount()),
                TooltipNode.empty(),
                Either.left(listing.price2()),
                new RangeValue(Math.max(1, listing.price2().getCount())),
                TooltipNode.empty(),
                Either.left(TooltipUtils.getItemStack(utils, listing.offer().copy(), getFunctions(listing))),
                new RangeValue(listing.offer().getCount()),
                utils.getValueTooltip(utils, listing.func()).build(Lang.Branch.MODIFIERS),
                listing.maxTrades(),
                listing.xp(),
                listing.priceMult(),
                condition
        );
    }

    @NotNull
    private static List<LootItemFunction> getFunctions(SimpleItemListing listing) {
        return listing.func() == null ? List.of() : List.of(listing.func());
    }
}

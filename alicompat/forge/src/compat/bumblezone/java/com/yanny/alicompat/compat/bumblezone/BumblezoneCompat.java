package com.yanny.alicompat.compat.bumblezone;

import com.telepathicgrunt.the_bumblezone.loot.conditions.EssenceOnlySpawn;
import com.telepathicgrunt.the_bumblezone.loot.forge.BeeStingerLootApplier;
import com.telepathicgrunt.the_bumblezone.loot.forge.DimensionFishingLootApplier;
import com.telepathicgrunt.the_bumblezone.loot.functions.DropContainerItems;
import com.telepathicgrunt.the_bumblezone.loot.functions.HoneyCompassLocateStructure;
import com.telepathicgrunt.the_bumblezone.loot.functions.TagItemRemovals;
import com.telepathicgrunt.the_bumblezone.loot.functions.UniquifyIfHasItems;
import com.telepathicgrunt.the_bumblezone.utils.GeneralUtils;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerRegistry;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.plugin.glm.IGlobalLootModifierPlugin;
import com.yanny.alicompat.IGlmModCompat;
import com.yanny.alicompat.accessor.GlmAccessorUtils;
import com.yanny.alicompat.accessor.PluginUtils;
import org.jetbrains.annotations.NotNull;

public class BumblezoneCompat implements IGlmModCompat {
    @NotNull
    @Override
    public String targetModId() {
        return BumblezoneLang.MOD_ID;
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        registry.registerConditionTooltip(EssenceOnlySpawn.class, BumblezoneCompat::getEssenceOnlySpawnTooltip);

        PluginUtils.registerFunctionTooltip(registry, DropContainerItems.class, DropContainerItemsAccessor::new);
        PluginUtils.registerFunctionTooltip(registry, UniquifyIfHasItems.class, UniquifyIfHasItemsAccessor::new);
        PluginUtils.registerFunctionTooltip(registry, TagItemRemovals.class, TagItemRemovalsAccessor.class);
        PluginUtils.registerFunctionTooltip(registry, HoneyCompassLocateStructure.class, HoneyCompassLocateStructureAccessor.class);

        PluginUtils.registerItemListing(registry, GeneralUtils.BasicItemTrade.class, BasicItemTradeAccessor.class);
    }

    @Override
    public void registerGlobalLootModifier(IGlobalLootModifierPlugin.IRegistry registry) {
        GlmAccessorUtils.registerGlobalLootModifier(registry, BeeStingerLootApplier.class, BeeStingerLootApplierAccessor.class);
        GlmAccessorUtils.registerGlobalLootModifier(registry, DimensionFishingLootApplier.class, DimensionFishingLootApplierAccessor.class);
    }

    @NotNull
    private static TooltipBuilder getEssenceOnlySpawnTooltip(IServerUtils ignoredUtils, EssenceOnlySpawn ignoredCond) {
        return TooltipBuilder.array(TooltipBuilder::showEmpty, BumblezoneLang.Conditions.ESSENCE_ONLY_SPAWN);
    }
}

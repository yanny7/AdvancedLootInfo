package com.yanny.alicompat.compat.bumblezone;

import com.telepathicgrunt.the_bumblezone.entities.BasicItemTrade;
import com.telepathicgrunt.the_bumblezone.entities.subpredicates.HoneySlimePredicate;
import com.telepathicgrunt.the_bumblezone.items.datacomponents.HoneyCompassBaseData;
import com.telepathicgrunt.the_bumblezone.items.datacomponents.HoneyCompassStateData;
import com.telepathicgrunt.the_bumblezone.items.datacomponents.HoneyCrystalShieldCurrentLevelData;
import com.telepathicgrunt.the_bumblezone.loot.conditions.EssenceOnlySpawn;
import com.telepathicgrunt.the_bumblezone.loot.functions.DropContainerLoot;
import com.telepathicgrunt.the_bumblezone.loot.functions.HoneyCompassLocateStructure;
import com.telepathicgrunt.the_bumblezone.loot.functions.TagItemRemovals;
import com.telepathicgrunt.the_bumblezone.loot.functions.UniquifyIfHasItems;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerRegistry;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import com.yanny.alicompat.IModCompat;
import com.yanny.alicompat.accessor.PluginUtils;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class BumblezoneCompat implements IModCompat {
    @NotNull
    @Override
    public String targetModId() {
        return BumblezoneLang.MOD_ID;
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        registry.registerConditionTooltip(EssenceOnlySpawn.class, BumblezoneCompat::getEssenceOnlySpawnTooltip);

        PluginUtils.registerFunctionTooltip(registry, DropContainerLoot.class, DropContainerLootAccessor::new);
        PluginUtils.registerFunctionTooltip(registry, UniquifyIfHasItems.class, UniquifyIfHasItemsAccessor::new);
        PluginUtils.registerFunctionTooltip(registry, TagItemRemovals.class, TagItemRemovalsAccessor.class);
        PluginUtils.registerFunctionTooltip(registry, HoneyCompassLocateStructure.class, HoneyCompassLocateStructureAccessor.class);

        PluginUtils.registerItemListing(registry, BasicItemTrade.class, BasicItemTradeAccessor.class);

        registry.registerEntitySubPredicateTooltip(HoneySlimePredicate.CODEC, BumblezoneCompat::getHoneySlimePredicateTooltip);

        registry.registerDataComponentTypeTooltip(getDataComponentType("honey_compass_base_data"), BumblezoneCompat::getHoneyCompassBaseDataTooltip);
        registry.registerDataComponentTypeTooltip(getDataComponentType("honey_compass_state_data"), BumblezoneCompat::getHoneyCompassStateDataTooltip);
        registry.registerDataComponentTypeTooltip(getDataComponentType("honey_crystal_shield_current_level_data"), BumblezoneCompat::getHoneyCrystalShieldCurrentLevelDataTooltip);
    }

    @SuppressWarnings("unchecked")
    @NotNull
    private static <T> DataComponentType<T> getDataComponentType(String path) {
        return (DataComponentType<T>) BuiltInRegistries.DATA_COMPONENT_TYPE.get(ResourceLocation.fromNamespaceAndPath(BumblezoneLang.MOD_ID, path));
    }

    @NotNull
    private static TooltipBuilder getEssenceOnlySpawnTooltip(IServerUtils ignoredUtils, EssenceOnlySpawn ignoredCond) {
        return TooltipBuilder.array(TooltipBuilder::showEmpty, BumblezoneLang.Conditions.ESSENCE_ONLY_SPAWN);
    }

    @NotNull
    private static TooltipBuilder getHoneySlimePredicateTooltip(IServerUtils utils, HoneySlimePredicate predicate) {
        return TooltipBuilder.array((b) -> b.add(utils.getValueTooltip(utils, predicate.isBaby()).build(Lang.Value.IS_BABY)), BumblezoneLang.EntitySubPredicates.HONEY_SLIME);
    }

    @NotNull
    private static TooltipBuilder getHoneyCompassBaseDataTooltip(IServerUtils utils, HoneyCompassBaseData value) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, value.compassType()).build(Lang.Value.TYPE));
            b.add(utils.getValueTooltip(utils, value.customName()).build(Lang.Value.CUSTOM_NAME));
            b.add(utils.getValueTooltip(utils, value.customDescription()).build(Lang.Value.DESCRIPTION));
        });
    }

    @NotNull
    private static TooltipBuilder getHoneyCompassStateDataTooltip(IServerUtils utils, HoneyCompassStateData value) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, value.locked()).build(BumblezoneLang.Value.LOCKED));
            b.add(utils.getValueTooltip(utils, value.searchId()).build(BumblezoneLang.Value.SEARCH_ID));
            b.add(utils.getValueTooltip(utils, value.isLoading()).build(BumblezoneLang.Value.IS_LOADING));
            b.add(utils.getValueTooltip(utils, value.isFailed()).build(BumblezoneLang.Value.IS_FAILED));
            b.add(utils.getValueTooltip(utils, value.locatedSpecialStructure()).build(BumblezoneLang.Value.LOCATED_SPECIAL_STRUCTURE));
        });
    }

    @NotNull
    private static TooltipBuilder getHoneyCrystalShieldCurrentLevelDataTooltip(IServerUtils utils, HoneyCrystalShieldCurrentLevelData value) {
        return utils.getValueTooltip(utils, value.currentLevel()).key(Lang.Value.LEVEL);
    }
}

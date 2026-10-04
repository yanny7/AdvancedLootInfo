package com.yanny.ali.lootjs.server;

import com.almostreliable.lootjs.core.filters.IdFilter;
import com.almostreliable.lootjs.core.filters.ItemFilter;
import com.almostreliable.lootjs.core.filters.ItemFilterImpl;
import com.almostreliable.lootjs.core.filters.ItemFilterWrapper;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import com.yanny.ali.plugin.server.MissingTooltipUtils;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.neoforge.common.ItemAbility;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class LootJsGenericTooltipUtils {
    @NotNull
    public static TooltipBuilder getItemFilterTooltip(IServerUtils utils, ItemFilter predicate) {
        if (predicate instanceof ItemFilterWrapper(ItemFilter filter)) {
            return getItemFilterTooltip(utils, filter);
        } else if (predicate == ItemFilter.NONE) {
            return filterTooltip(Lang.ItemFilter.NONE);
        } else if (predicate == ItemFilter.ANY) {
            return filterTooltip(Lang.ItemFilter.ANY);
        } else if (predicate == ItemFilter.EMPTY) {
            return filterTooltip(Lang.ItemFilter.EMPTY);
        } else if (predicate == ItemFilter.ARMOR) {
            return filterTooltip(Lang.ItemFilter.ARMOR);
        } else if (predicate == ItemFilter.EDIBLE) {
            return filterTooltip(Lang.ItemFilter.EDIBLE);
        } else if (predicate == ItemFilter.DAMAGEABLE) {
            return filterTooltip(Lang.ItemFilter.DAMAGEABLE);
        } else if (predicate == ItemFilter.DAMAGED) {
            return filterTooltip(Lang.ItemFilter.DAMAGED);
        } else if (predicate == ItemFilter.ENCHANTED) {
            return filterTooltip(Lang.ItemFilter.ENCHANTED);
        } else if (predicate == ItemFilter.BLOCK_ITEM) {
            return filterTooltip(Lang.ItemFilter.BLOCK_ITEM);
        } else if (predicate instanceof ItemFilterImpl.HasEnchantment(IdFilter filter, MinMaxBounds.Ints levelBounds, DataComponentType<ItemEnchantments> type)) {
            return filterTooltip(Lang.ItemFilter.HAS_ENCHANTMENT)
                    .add(utils.getValueTooltip(utils, filter).build(Lang.Branch.FILTER))
                    .add(utils.getValueTooltip(utils, levelBounds).build(Lang.Value.LEVELS))
                    .add(utils.getValueTooltip(utils, type).build(Lang.Value.COMPONENT));
        } else if (predicate instanceof ItemFilterImpl.HasComponent hasComponent) {
            return filterTooltip(Lang.ItemFilter.HAS_COMPONENT)
                    .add(utils.getValueTooltip(utils, List.of(hasComponent.types())).build(Lang.Branch.COMPONENTS));
        } else if (predicate instanceof ItemFilterImpl.IsEquipmentSlot(EquipmentSlot equipmentSlot)) {
            return filterTooltip(Lang.ItemFilter.EQUIPMENT_SLOT)
                    .add(utils.getValueTooltip(utils, equipmentSlot).build(Lang.Value.SLOT));
        } else if (predicate instanceof ItemFilterImpl.IsEquipmentSlotGroup(EquipmentSlotGroup equipmentSlotGroup)) {
            return filterTooltip(Lang.ItemFilter.EQUIPMENT_SLOT_GROUP)
                    .add(utils.getValueTooltip(utils, equipmentSlotGroup).build(Lang.Value.SLOT_GROUP));
        } else if (predicate instanceof ItemFilterImpl.ByItem(ItemStack itemStack, boolean checkComponents)) {
            return filterTooltip(Lang.ItemFilter.ITEM)
                    .add(utils.getValueTooltip(utils, itemStack).build(Lang.Branch.ITEM))
                    .add(utils.getValueTooltip(utils, checkComponents).build(Lang.Value.CHECK_COMPONENTS));
        } else if (predicate instanceof ItemFilterImpl.ByIngredient(Ingredient ingredient)) {
            return filterTooltip(Lang.ItemFilter.INGREDIENT)
                    .add(utils.getValueTooltip(utils, ingredient));
        } else if (predicate instanceof ItemFilterImpl.ByTag(TagKey<Item> tag)) {
            return filterTooltip(Lang.ItemFilter.TAG)
                    .add(utils.getValueTooltip(utils, tag).build());
        } else if (predicate instanceof ItemFilterImpl.AnyOfToolAction toolAction) {
            return filterTooltip(Lang.ItemFilter.ANY_OF_TOOL_ACTION)
                    .add(utils.getValueTooltip(utils, toolAction.toolActions()).build(Lang.Branch.ABILITIES));
        } else if (predicate instanceof ItemFilterImpl.AllOfToolAction toolAction) {
            return filterTooltip(Lang.ItemFilter.ALL_OF_TOOL_ACTION)
                    .add(utils.getValueTooltip(utils, toolAction.toolActions()).build(Lang.Branch.ABILITIES));
        } else if (predicate instanceof ItemFilterImpl.Not(ItemFilter itemFilter)) {
            return filterTooltip(Lang.ItemFilter.NOT)
                    .add(utils.getValueTooltip(utils, itemFilter).build(Lang.Value.ITEM_FILTER));
        } else if (predicate instanceof ItemFilterImpl.AllOf allOf) {
            return filterTooltip(Lang.ItemFilter.ALL_OF)
                    .add(utils.getValueTooltip(utils, List.of(allOf.itemFilters())).build(Lang.Branch.FILTERS));
        } else if (predicate instanceof ItemFilterImpl.AnyOf allOf) {
            return filterTooltip(Lang.ItemFilter.ANY_OF)
                    .add(utils.getValueTooltip(utils, List.of(allOf.itemFilters())).build(Lang.Branch.FILTERS));
        } else if (predicate instanceof ItemFilterImpl.Custom custom) {
            return filterTooltip(Lang.ItemFilter.CUSTOM)
                    .add(utils.getValueTooltip(utils, Optional.ofNullable(custom.description())).build(Lang.Value.DESCRIPTION));
        }

        return filterTooltip(Lang.ItemFilter.UNKNOWN);
    }

    @NotNull
    private static TooltipBuilder filterTooltip(Lang.ItemFilter filter) {
        return TooltipBuilder.value(TooltipBuilder.translate(filter.singular()));
    }

    @NotNull
    public static TooltipBuilder getItemFilterWrapperTooltip(IServerUtils utils, ItemFilterWrapper predicate) {
        return getItemFilterTooltip(utils, predicate.filter()).key(Lang.Value.ITEM_FILTER);
    }

    @NotNull
    public static TooltipBuilder getIdFilterTooltip(IServerUtils utils, IdFilter filter) {
        return switch (filter) {
            case IdFilter.ByLocation byLocation -> TooltipBuilder.array((b) -> b.add(utils.getValueTooltip(utils, byLocation.location())));
            case IdFilter.ByPattern byPattern -> TooltipBuilder.array((b) -> b.add(utils.getValueTooltip(utils, byPattern.pattern().pattern()).build(Lang.Value.PATTERN)));
            case IdFilter.ByMod byMod -> TooltipBuilder.array((b) -> b.add(utils.getValueTooltip(utils, byMod.mod()).build(Lang.Value.MOD)));
            case IdFilter.Or or -> TooltipBuilder.array((b) -> b.add(utils.getValueTooltip(utils, or.filters()).build(Lang.Branch.OR)));
            default -> MissingTooltipUtils.getMissingValueTooltip(utils, filter);
        };
    }

    @NotNull
    public static TooltipBuilder getItemAbilityTooltip(IServerUtils utils, ItemAbility itemAbility) {
        return utils.getValueTooltip(utils, itemAbility.name());
    }
}

package com.yanny.ali.lootjs.server;

import com.almostreliable.lootjs.core.filters.IdFilter;
import com.almostreliable.lootjs.core.filters.ItemFilter;
import com.almostreliable.lootjs.core.filters.ItemFilterImpl;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerUtils;
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

import java.util.Arrays;
import java.util.Optional;

import static com.yanny.ali.plugin.server.GenericTooltipUtils.getCollectionTooltip;

public class LootJsGenericTooltipUtils {
    @NotNull
    public static TooltipBuilder getItemFilterTooltip(IServerUtils utils, ItemFilter predicate) {
        if (predicate == ItemFilter.NONE) {
            return TooltipBuilder.value("NONE");
        } else if (predicate == ItemFilter.ANY) {
            return TooltipBuilder.value("ANY");
        } else if (predicate == ItemFilter.EMPTY) {
            return TooltipBuilder.value("EMPTY");
        } else if (predicate == ItemFilter.ARMOR) {
            return TooltipBuilder.value("ARMOR");
        } else if (predicate == ItemFilter.EDIBLE) {
            return TooltipBuilder.value("EDIBLE");
        } else if (predicate == ItemFilter.DAMAGEABLE) {
            return TooltipBuilder.value("DAMAGEABLE");
        } else if (predicate == ItemFilter.DAMAGED) {
            return TooltipBuilder.value("ENCHANTABLE");
        } else if (predicate == ItemFilter.ENCHANTED) {
            return TooltipBuilder.value("ENCHANTED");
        } else if (predicate == ItemFilter.BLOCK_ITEM) {
            return TooltipBuilder.value("BLOCK_ITEM");
        } else if (predicate instanceof ItemFilterImpl.HasEnchantment(IdFilter filter, MinMaxBounds.Ints levelBounds, DataComponentType<ItemEnchantments> type)) {
            return TooltipBuilder.value("HAS_ENCHANTMENT")
                    .add(utils.getValueTooltip(utils, filter).build("ali.property.branch.filter"))
                    .add(utils.getValueTooltip(utils, levelBounds).build("ali.property.value.levels"))
                    .add(utils.getValueTooltip(utils, type).build("ali.property.value.component"));
        } else if (predicate instanceof ItemFilterImpl.IsEquipmentSlot(EquipmentSlot equipmentSlot)) {
            return TooltipBuilder.value("EQUIPMENT_SLOT")
                    .add(utils.getValueTooltip(utils, equipmentSlot).build("ali.property.value.slot"));
        } else if (predicate instanceof ItemFilterImpl.IsEquipmentSlotGroup(EquipmentSlotGroup equipmentSlotGroup)) {
            return TooltipBuilder.value("EQUIPMENT_SLOT_GROUP")
                    .add(utils.getValueTooltip(utils, equipmentSlotGroup).build("ali.property.value.slot_group"));
        } else if (predicate instanceof ItemFilterImpl.ByItem(ItemStack itemStack, boolean checkComponents)) {
            return TooltipBuilder.value("ITEM")
                    .add(utils.getValueTooltip(utils, itemStack).build("ali.property.branch.item"))
                    .add(utils.getValueTooltip(utils, checkComponents).build("ali.property.value.check_components"));
        } else if (predicate instanceof ItemFilterImpl.ByIngredient(Ingredient ingredient)) {
            return TooltipBuilder.value("INGREDIENT")
                    .add(utils.getIngredientTooltip(utils, ingredient));
        } else if (predicate instanceof ItemFilterImpl.ByTag(TagKey<Item> tag)) {
            return TooltipBuilder.value("TAG")
                    .add(utils.getValueTooltip(utils, tag).build("aci.util.null"));
        } else if (predicate instanceof ItemFilterImpl.AnyOfToolAction toolAction) {
            return TooltipBuilder.value("ANY_OF_TOOL_ACTION")
                    .add(utils.getValueTooltip(utils, toolAction.toolActions()).build("ali.property.branch.abilities"));
        } else if (predicate instanceof ItemFilterImpl.AllOfToolAction toolAction) {
            return TooltipBuilder.value("ALL_OF_TOOL_ACTION")
                    .add(utils.getValueTooltip(utils, toolAction.toolActions()).build("ali.property.branch.abilities"));
        } else if (predicate instanceof ItemFilterImpl.Not(ItemFilter itemFilter)) {
            return TooltipBuilder.value("NOT")
                    .add(utils.getValueTooltip(utils, itemFilter).build("ali.property.branch.filter"));
        } else if (predicate instanceof ItemFilterImpl.AllOf allOf) {
            return TooltipBuilder.value("ALL_OF")
                    .add(getCollectionTooltip(utils, "ali.property.value.filter", Arrays.asList(allOf.itemFilters())).build("ali.property.branch.filters"));
        } else if (predicate instanceof ItemFilterImpl.AnyOf allOf) {
            return TooltipBuilder.value("ANY_OF")
                    .add(getCollectionTooltip(utils, "ali.property.value.filter", Arrays.asList(allOf.itemFilters())).build("ali.property.branch.filters"));
        } else if (predicate instanceof ItemFilterImpl.Custom custom) {
            return TooltipBuilder.value("CUSTOM")
                    .add(utils.getValueTooltip(utils, Optional.ofNullable(custom.description())).build("ali.property.value.description"));
        }

        return TooltipBuilder.value("UNKNOWN");
    }

    @NotNull
    public static TooltipBuilder getIdFilterTooltip(IServerUtils utils, IdFilter filter) {
        return switch (filter) {
            case IdFilter.ByLocation byLocation -> utils.getValueTooltip(utils, byLocation.location());
            case IdFilter.ByPattern byPattern -> utils.getValueTooltip(utils, byPattern.toString()).key("ali.property.value.pattern");
            case IdFilter.ByMod byMod -> utils.getValueTooltip(utils, byMod.mod()).key("ali.property.value.mod");
            case IdFilter.Or or -> utils.getValueTooltip(utils, or.filters()).key("ali.property.branch.or");
            default -> throw new IllegalStateException("Unexpected IdFilter type: " + filter);
        };
    }

    @NotNull
    public static TooltipBuilder getItemAbilityTooltip(IServerUtils utils, ItemAbility itemAbility) {
        return utils.getValueTooltip(utils, itemAbility.name());
    }
}

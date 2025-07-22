package com.yanny.ali.plugin.lootjs.server;

import com.almostreliable.lootjs.core.filters.IdFilter;
import com.almostreliable.lootjs.core.filters.ItemFilter;
import com.almostreliable.lootjs.core.filters.ItemFilterImpl;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import com.yanny.ali.api.TooltipNode;
import com.yanny.ali.plugin.server.GenericTooltipUtils;
import net.minecraft.advancements.critereon.MinMaxBounds;
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

import static com.yanny.ali.plugin.server.GenericTooltipUtils.*;
import static com.yanny.ali.plugin.server.RegistriesTooltipUtils.getDataComponentTypeTooltip;

public class LootJsGenericTooltipUtils {
    @NotNull
    public static ITooltipNode getItemFilterTooltip(IServerUtils utils, String key, ItemFilter predicate) {
        if (predicate == ItemFilter.NONE) {
            return new TooltipNode(translatable(key, value("NONE")));
        } else if (predicate == ItemFilter.ANY) {
            return new TooltipNode(translatable(key, value("ANY")));
        } else if (predicate == ItemFilter.EMPTY) {
            return new TooltipNode(translatable(key, value("EMPTY")));
        } else if (predicate == ItemFilter.ARMOR) {
            return new TooltipNode(translatable(key, value("ARMOR")));
        } else if (predicate == ItemFilter.EDIBLE) {
            return new TooltipNode(translatable(key, value("EDIBLE")));
        } else if (predicate == ItemFilter.DAMAGEABLE) {
            return new TooltipNode(translatable(key, value("DAMAGEABLE")));
        } else if (predicate == ItemFilter.DAMAGED) {
            return new TooltipNode(translatable(key, value("ENCHANTABLE")));
        } else if (predicate == ItemFilter.ENCHANTED) {
            return new TooltipNode(translatable(key, value("ENCHANTED")));
        } else if (predicate == ItemFilter.BLOCK_ITEM) {
            return new TooltipNode(translatable(key, value("BLOCK_ITEM")));
        } else if (predicate instanceof ItemFilterImpl.HasEnchantment(IdFilter filter, MinMaxBounds.Ints levelBounds, DataComponentType<ItemEnchantments> type)) {
            ITooltipNode tooltip = new TooltipNode(translatable(key, value("HAS_ENCHANTMENT")));

            tooltip.add(getIdFilterTooltip(utils, "ali.property.branch.filter", filter));
            tooltip.add(getMinMaxBoundsTooltip(utils, "ali.property.value.levels", levelBounds));
            tooltip.add(getDataComponentTypeTooltip(utils, "ali.property.value.component", type));

            return tooltip;
        } else if (predicate instanceof ItemFilterImpl.IsEquipmentSlot(EquipmentSlot equipmentSlot)) {
            ITooltipNode tooltip = new TooltipNode(translatable(key, value("EQUIPMENT_SLOT")));

            tooltip.add(getEnumTooltip(utils, "ali.property.value.slot", equipmentSlot));

            return tooltip;
        } else if (predicate instanceof ItemFilterImpl.IsEquipmentSlotGroup(EquipmentSlotGroup equipmentSlotGroup)) {
            ITooltipNode tooltip = new TooltipNode(translatable(key, value("EQUIPMENT_SLOT_GROUP")));

            tooltip.add(getEnumTooltip(utils, "ali.property.value.slot_group", equipmentSlotGroup));

            return tooltip;
        } else if (predicate instanceof ItemFilterImpl.ByItem(ItemStack itemStack, boolean checkComponents)) {
            ITooltipNode tooltip = new TooltipNode(translatable(key, value("ITEM")));

            tooltip.add(getItemStackTooltip(utils, "ali.property.branch.item", itemStack));
            tooltip.add(getBooleanTooltip(utils, "ali.property.value.check_components", checkComponents));

            return tooltip;
        } else if (predicate instanceof ItemFilterImpl.ByIngredient(Ingredient ingredient)) {
            ITooltipNode tooltip = new TooltipNode(translatable(key, value("INGREDIENT")));

            tooltip.add(utils.getIngredientTooltip(utils, ingredient));

            return tooltip;
        } else if (predicate instanceof ItemFilterImpl.ByTag(TagKey<Item> tag)) {
            ITooltipNode tooltip = new TooltipNode(translatable(key, value("TAG")));

            tooltip.add(getTagKeyTooltip(utils, "ali.property.value.null", tag));

            return tooltip;
        } else if (predicate instanceof ItemFilterImpl.AnyOfToolAction toolAction) {
            ITooltipNode tooltip = new TooltipNode(translatable(key, value("ANY_OF_TOOL_ACTION")));

            tooltip.add(getCollectionTooltip(utils, "ali.property.branch.abilities", "ali.property.value.null", toolAction.toolActions(), LootJsGenericTooltipUtils::getItemAbilityTooltip));

            return tooltip;
        } else if (predicate instanceof ItemFilterImpl.AllOfToolAction toolAction) {
            ITooltipNode tooltip = new TooltipNode(translatable(key, value("ALL_OF_TOOL_ACTION")));

            tooltip.add(getCollectionTooltip(utils, "ali.property.branch.abilities", "ali.property.value.null", toolAction.toolActions(), LootJsGenericTooltipUtils::getItemAbilityTooltip));

            return tooltip;
        } else if (predicate instanceof ItemFilterImpl.Not(ItemFilter itemFilter)) {
            ITooltipNode tooltip = new TooltipNode(translatable(key, value("NOT")));

            tooltip.add(getItemFilterTooltip(utils, "ali.property.branch.filter", itemFilter));

            return tooltip;
        } else if (predicate instanceof ItemFilterImpl.AllOf allOf) {
            ITooltipNode tooltip = new TooltipNode(translatable(key, value("ALL_OF")));

            tooltip.add(getCollectionTooltip(utils, "ali.property.branch.filters", "ali.property.value.filter", Arrays.asList(allOf.itemFilters()), LootJsGenericTooltipUtils::getItemFilterTooltip));

            return tooltip;
        } else if (predicate instanceof ItemFilterImpl.AnyOf allOf) {
            ITooltipNode tooltip = new TooltipNode(translatable(key, value("ANY_OF")));

            tooltip.add(getCollectionTooltip(utils, "ali.property.branch.filters", "ali.property.value.filter", Arrays.asList(allOf.itemFilters()), LootJsGenericTooltipUtils::getItemFilterTooltip));

            return tooltip;
        } else if (predicate instanceof ItemFilterImpl.Custom custom) {
            ITooltipNode tooltip = new TooltipNode(translatable(key, value("CUSTOM")));

            tooltip.add(getOptionalTooltip(utils, "ali.property.value.description", Optional.ofNullable(custom.description()), GenericTooltipUtils::getStringTooltip));

            return tooltip;
        }

        return new TooltipNode(translatable(key, value("UNKNOWN")));
    }

    @NotNull
    private static ITooltipNode getIdFilterTooltip(IServerUtils utils, String key, IdFilter filter) {
        ITooltipNode tooltip = new TooltipNode(translatable(key));

        switch (filter) {
            case IdFilter.ByLocation byLocation -> tooltip.add(getResourceLocationTooltip(utils, "ali.property.value.null", byLocation.location()));
            case IdFilter.ByPattern byPattern -> tooltip.add(getStringTooltip(utils, "ali.property.value.pattern", byPattern.toString()));
            case IdFilter.ByMod byMod -> tooltip.add(getStringTooltip(utils, "ali.property.value.mod", byMod.mod()));
            case IdFilter.Or or -> tooltip.add(getCollectionTooltip(utils, "ali.property.branch.or", "ali.property.value.null", or.filters(), LootJsGenericTooltipUtils::getIdFilterTooltip));
            default -> throw new IllegalStateException("Unexpected IdFilter type: " + filter);
        }

        return tooltip;
    }

    @NotNull
    private static ITooltipNode getItemAbilityTooltip(IServerUtils utils, String key, ItemAbility itemAbility) {
        return getStringTooltip(utils, key, itemAbility.name());
    }
}

package com.yanny.ali.plugin.lootjs.server;

import com.almostreliable.lootjs.core.filters.ItemFilter;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import com.yanny.ali.api.TooltipNode;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;

import static com.yanny.ali.plugin.server.GenericTooltipUtils.*;

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
        } else if (predicate instanceof ItemFilter.Tag(TagKey<Item> tag)) {
            return getTagKeyTooltip(utils, key, tag);
        } else if (predicate instanceof ItemFilter.Ingredient(Ingredient ingredient)) {
            return utils.getIngredientTooltip(utils, ingredient);
        }

        return new TooltipNode(translatable(key, value("UNKNOWN")));
    }
}

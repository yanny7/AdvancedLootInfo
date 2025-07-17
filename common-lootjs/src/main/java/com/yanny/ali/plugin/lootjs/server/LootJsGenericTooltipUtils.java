package com.yanny.ali.plugin.lootjs.server;

import com.almostreliable.lootjs.filters.ItemFilter;
import com.almostreliable.lootjs.filters.ResourceLocationFilter;
import com.almostreliable.lootjs.loot.condition.AnyStructure;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import com.yanny.ali.api.TooltipNode;
import com.yanny.ali.plugin.lootjs.Utils;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.storage.loot.IntRange;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Predicate;

import static com.yanny.ali.plugin.server.GenericTooltipUtils.*;

public class LootJsGenericTooltipUtils {
    @NotNull
    public static ITooltipNode getItemFilterTooltip(IServerUtils utils, String key, Predicate<ItemStack> predicate) {
        if (predicate instanceof ItemFilter) {
            if (predicate == ItemFilter.ALWAYS_FALSE) {
                return new TooltipNode(translatable(key, value("ALWAYS_FALSE")));
            } else if (predicate == ItemFilter.ALWAYS_TRUE) {
                return new TooltipNode(translatable(key, value("ALWAYS_TRUE")));
            } else if (predicate == ItemFilter.SWORD) {
                return new TooltipNode(translatable(key, value("SWORD")));
            } else if (predicate == ItemFilter.PICKAXE) {
                return new TooltipNode(translatable(key, value("PICKAXE")));
            } else if (predicate == ItemFilter.AXE) {
                return new TooltipNode(translatable(key, value("AXE")));
            } else if (predicate == ItemFilter.SHOVEL) {
                return new TooltipNode(translatable(key, value("SHOVEL")));
            } else if (predicate == ItemFilter.HOE) {
                return new TooltipNode(translatable(key, value("HOE")));
            } else if (predicate == ItemFilter.TOOL) {
                return new TooltipNode(translatable(key, value("TOOL")));
            } else if (predicate == ItemFilter.POTION) {
                return new TooltipNode(translatable(key, value("POTION")));
            } else if (predicate == ItemFilter.HAS_TIER) {
                return new TooltipNode(translatable(key, value("HAS_TIER")));
            } else if (predicate == ItemFilter.PROJECTILE_WEAPON) {
                return new TooltipNode(translatable(key, value("PROJECTILE_WEAPON")));
            } else if (predicate == ItemFilter.ARMOR) {
                return new TooltipNode(translatable(key, value("ARMOR")));
            } else if (predicate == ItemFilter.WEAPON) {
                return new TooltipNode(translatable(key, value("WEAPON")));
            } else if (predicate == ItemFilter.HEAD_ARMOR) {
                return new TooltipNode(translatable(key, value("HEAD_ARMOR")));
            } else if (predicate == ItemFilter.CHEST_ARMOR) {
                return new TooltipNode(translatable(key, value("CHEST_ARMOR")));
            } else if (predicate == ItemFilter.LEGS_ARMOR) {
                return new TooltipNode(translatable(key, value("LEGS_ARMOR")));
            } else if (predicate == ItemFilter.FEET_ARMOR) {
                return new TooltipNode(translatable(key, value("FEET_ARMOR")));
            } else if (predicate == ItemFilter.FOOD) {
                return new TooltipNode(translatable(key, value("FOOD")));
            } else if (predicate == ItemFilter.DAMAGEABLE) {
                return new TooltipNode(translatable(key, value("DAMAGEABLE")));
            } else if (predicate == ItemFilter.DAMAGED) {
                return new TooltipNode(translatable(key, value("DAMAGED")));
            } else if (predicate == ItemFilter.ENCHANTABLE) {
                return new TooltipNode(translatable(key, value("ENCHANTABLE")));
            } else if (predicate == ItemFilter.ENCHANTED) {
                return new TooltipNode(translatable(key, value("ENCHANTED")));
            } else if (predicate == ItemFilter.BLOCK) {
                return new TooltipNode(translatable(key, value("BLOCK")));
            }

            List<ResourceLocationFilter.ByLocation> byLocation = Utils.getCapturedInstances(predicate, ResourceLocationFilter.ByLocation.class);

            if (byLocation.size() == 1) {
                List<Integer> minMax = Utils.getCapturedInstances(predicate, Integer.class);

                if (minMax.size() == 2) {
                    ITooltipNode tooltip = new TooltipNode(translatable(key, value("HAS_ENCHANTMENT")));
                    int min = Math.min(minMax.get(0), minMax.get(1));
                    int max = Math.max(minMax.get(0), minMax.get(1));

                    tooltip.add(getResourceLocationTooltip(utils, "ali.property.value.enchantment", byLocation.get(0).location()));

                    if (min != 1 && max != 255) {
                        tooltip.add(getIntRangeTooltip(utils, "ali.property.levels", IntRange.range(min, max)));
                    }

                    return tooltip;
                }
            }

            List<ResourceLocationFilter.ByPattern> byPattern = Utils.getCapturedInstances(predicate, ResourceLocationFilter.ByPattern.class);

            if (byPattern.size() == 1) {
                List<Integer> minMax = Utils.getCapturedInstances(predicate, Integer.class);

                if (minMax.size() == 2) {
                    ITooltipNode tooltip = new TooltipNode(translatable(key, value("HAS_ENCHANTMENT")));
                    int min = Math.min(minMax.get(0), minMax.get(1));
                    int max = Math.max(minMax.get(0), minMax.get(1));

                    tooltip.add(getStringTooltip(utils, "ali.property.value.enchantment", byPattern.get(0).toString()));

                    if (min != 1 && max != 255) {
                        tooltip.add(getIntRangeTooltip(utils, "ali.property.levels", IntRange.range(min, max)));
                    }

                    return tooltip;
                }
            }

            List<Ingredient> ingredient = Utils.getCapturedInstances(predicate, Ingredient.class);

            if (ingredient.size() == 1) {
                Ingredient i = ingredient.get(0);

                if (!i.isEmpty()) {
                    ITooltipNode tooltip = new TooltipNode(translatable(key, value("INGREDIENT")));

                    tooltip.add(utils.getIngredientTooltip(utils, i));

                    return tooltip;
                }
            }
        }


        return new TooltipNode(translatable(key, value("UNKNOWN")));
    }

    @NotNull
    public static ITooltipNode getStructureLocatorTooltip(IServerUtils utils, String key, AnyStructure.StructureLocator structureLocator) {
        if (structureLocator instanceof AnyStructure.ById byId) {
            return getResourceKeyTooltip(utils, key, byId.id());
        } else if (structureLocator instanceof AnyStructure.ByTag byTag) {
            return getTagKeyTooltip(utils, key, byTag.tag());
        }

        return TooltipNode.EMPTY;
    }
}

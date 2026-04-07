package com.yanny.ali.lootjs.server;

import com.almostreliable.lootjs.filters.ItemFilter;
import com.almostreliable.lootjs.filters.ResourceLocationFilter;
import com.almostreliable.lootjs.loot.condition.AnyStructure;
import com.yanny.ali.api.IKeyTooltipNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.plugin.common.tooltip.EmptyTooltipNode;
import com.yanny.ali.plugin.common.tooltip.ValueTooltipNode;
import com.yanny.ali.plugin.mods.PluginUtils;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.storage.loot.IntRange;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Predicate;

public class LootJsGenericTooltipUtils {
    @NotNull
    public static IKeyTooltipNode getItemFilterTooltip(IServerUtils utils, Predicate<ItemStack> predicate) {
        if (predicate instanceof ItemFilter) {
            if (predicate == ItemFilter.ALWAYS_FALSE) {
                return ValueTooltipNode.value("ALWAYS_FALSE");
            } else if (predicate == ItemFilter.ALWAYS_TRUE) {
                return ValueTooltipNode.value("ALWAYS_TRUE");
            } else if (predicate == ItemFilter.SWORD) {
                return ValueTooltipNode.value("SWORD");
            } else if (predicate == ItemFilter.PICKAXE) {
                return ValueTooltipNode.value("PICKAXE");
            } else if (predicate == ItemFilter.AXE) {
                return ValueTooltipNode.value("AXE");
            } else if (predicate == ItemFilter.SHOVEL) {
                return ValueTooltipNode.value("SHOVEL");
            } else if (predicate == ItemFilter.HOE) {
                return ValueTooltipNode.value("HOE");
            } else if (predicate == ItemFilter.TOOL) {
                return ValueTooltipNode.value("TOOL");
            } else if (predicate == ItemFilter.POTION) {
                return ValueTooltipNode.value("POTION");
            } else if (predicate == ItemFilter.HAS_TIER) {
                return ValueTooltipNode.value("HAS_TIER");
            } else if (predicate == ItemFilter.PROJECTILE_WEAPON) {
                return ValueTooltipNode.value("PROJECTILE_WEAPON");
            } else if (predicate == ItemFilter.ARMOR) {
                return ValueTooltipNode.value("ARMOR");
            } else if (predicate == ItemFilter.WEAPON) {
                return ValueTooltipNode.value("WEAPON");
            } else if (predicate == ItemFilter.HEAD_ARMOR) {
                return ValueTooltipNode.value("HEAD_ARMOR");
            } else if (predicate == ItemFilter.CHEST_ARMOR) {
                return ValueTooltipNode.value("CHEST_ARMOR");
            } else if (predicate == ItemFilter.LEGS_ARMOR) {
                return ValueTooltipNode.value("LEGS_ARMOR");
            } else if (predicate == ItemFilter.FEET_ARMOR) {
                return ValueTooltipNode.value("FEET_ARMOR");
            } else if (predicate == ItemFilter.FOOD) {
                return ValueTooltipNode.value("FOOD");
            } else if (predicate == ItemFilter.DAMAGEABLE) {
                return ValueTooltipNode.value("DAMAGEABLE");
            } else if (predicate == ItemFilter.DAMAGED) {
                return ValueTooltipNode.value("DAMAGED");
            } else if (predicate == ItemFilter.ENCHANTABLE) {
                return ValueTooltipNode.value("ENCHANTABLE");
            } else if (predicate == ItemFilter.ENCHANTED) {
                return ValueTooltipNode.value("ENCHANTED");
            } else if (predicate == ItemFilter.BLOCK) {
                return ValueTooltipNode.value("BLOCK");
            }

            List<ResourceLocationFilter.ByLocation> byLocation = PluginUtils.getCapturedInstances(predicate, ResourceLocationFilter.ByLocation.class);

            if (byLocation.size() == 1) {
                List<Integer> minMax = PluginUtils.getCapturedInstances(predicate, Integer.class);

                if (minMax.size() == 2) {
                    IKeyTooltipNode tooltip = ValueTooltipNode.value("HAS_ENCHANTMENT");
                    int min = Math.min(minMax.get(0), minMax.get(1));
                    int max = Math.max(minMax.get(0), minMax.get(1));

                    tooltip.add(utils.getValueTooltip(utils, byLocation.get(0).location()).build("ali.property.value.enchantment"));

                    if (min != 1 && max != 255) {
                        tooltip.add(utils.getValueTooltip(utils, IntRange.range(min, max)).build("ali.property.value.levels"));
                    }

                    return tooltip;
                }
            }

            List<ResourceLocationFilter.ByPattern> byPattern = PluginUtils.getCapturedInstances(predicate, ResourceLocationFilter.ByPattern.class);

            if (byPattern.size() == 1) {
                List<Integer> minMax = PluginUtils.getCapturedInstances(predicate, Integer.class);

                if (minMax.size() == 2) {
                    IKeyTooltipNode tooltip = ValueTooltipNode.value("HAS_ENCHANTMENT");
                    int min = Math.min(minMax.get(0), minMax.get(1));
                    int max = Math.max(minMax.get(0), minMax.get(1));

                    tooltip.add(utils.getValueTooltip(utils, byPattern.get(0).toString()).build("ali.property.value.enchantment"));

                    if (min != 1 && max != 255) {
                        tooltip.add(utils.getValueTooltip(utils, IntRange.range(min, max)).build("ali.property.value.levels"));
                    }

                    return tooltip;
                }
            }

            List<Ingredient> ingredient = PluginUtils.getCapturedInstances(predicate, Ingredient.class);

            if (ingredient.size() == 1) {
                Ingredient i = ingredient.get(0);

                if (!i.isEmpty()) {
                    return ValueTooltipNode.value("INGREDIENT")
                            .add(utils.getIngredientTooltip(utils, i));
                }
            }
        }

        return ValueTooltipNode.value("UNKNOWN");
    }

    @NotNull
    public static IKeyTooltipNode getStructureLocatorTooltip(IServerUtils utils, AnyStructure.StructureLocator structureLocator) {
        if (structureLocator instanceof AnyStructure.ById byId) {
            return utils.getValueTooltip(utils, byId.id());
        } else if (structureLocator instanceof AnyStructure.ByTag byTag) {
            return utils.getValueTooltip(utils, byTag.tag());
        }

        return EmptyTooltipNode.empty();
    }
}

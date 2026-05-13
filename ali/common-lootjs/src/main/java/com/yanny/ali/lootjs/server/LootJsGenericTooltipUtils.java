package com.yanny.ali.lootjs.server;

import com.almostreliable.lootjs.filters.ItemFilter;
import com.almostreliable.lootjs.filters.ResourceLocationFilter;
import com.almostreliable.lootjs.loot.condition.AnyStructure;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import com.yanny.ali.plugin.mods.PluginUtils;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.storage.loot.IntRange;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Predicate;

public class LootJsGenericTooltipUtils {
    @NotNull
    public static TooltipBuilder getItemFilterTooltip(IServerUtils utils, Predicate<ItemStack> predicate) {
        if (predicate instanceof ItemFilter) {
            if (predicate == ItemFilter.ALWAYS_FALSE) {
                return TooltipBuilder.value("ALWAYS_FALSE");
            } else if (predicate == ItemFilter.ALWAYS_TRUE) {
                return TooltipBuilder.value("ALWAYS_TRUE");
            } else if (predicate == ItemFilter.SWORD) {
                return TooltipBuilder.value("SWORD");
            } else if (predicate == ItemFilter.PICKAXE) {
                return TooltipBuilder.value("PICKAXE");
            } else if (predicate == ItemFilter.AXE) {
                return TooltipBuilder.value("AXE");
            } else if (predicate == ItemFilter.SHOVEL) {
                return TooltipBuilder.value("SHOVEL");
            } else if (predicate == ItemFilter.HOE) {
                return TooltipBuilder.value("HOE");
            } else if (predicate == ItemFilter.TOOL) {
                return TooltipBuilder.value("TOOL");
            } else if (predicate == ItemFilter.POTION) {
                return TooltipBuilder.value("POTION");
            } else if (predicate == ItemFilter.HAS_TIER) {
                return TooltipBuilder.value("HAS_TIER");
            } else if (predicate == ItemFilter.PROJECTILE_WEAPON) {
                return TooltipBuilder.value("PROJECTILE_WEAPON");
            } else if (predicate == ItemFilter.ARMOR) {
                return TooltipBuilder.value("ARMOR");
            } else if (predicate == ItemFilter.WEAPON) {
                return TooltipBuilder.value("WEAPON");
            } else if (predicate == ItemFilter.HEAD_ARMOR) {
                return TooltipBuilder.value("HEAD_ARMOR");
            } else if (predicate == ItemFilter.CHEST_ARMOR) {
                return TooltipBuilder.value("CHEST_ARMOR");
            } else if (predicate == ItemFilter.LEGS_ARMOR) {
                return TooltipBuilder.value("LEGS_ARMOR");
            } else if (predicate == ItemFilter.FEET_ARMOR) {
                return TooltipBuilder.value("FEET_ARMOR");
            } else if (predicate == ItemFilter.FOOD) {
                return TooltipBuilder.value("FOOD");
            } else if (predicate == ItemFilter.DAMAGEABLE) {
                return TooltipBuilder.value("DAMAGEABLE");
            } else if (predicate == ItemFilter.DAMAGED) {
                return TooltipBuilder.value("DAMAGED");
            } else if (predicate == ItemFilter.ENCHANTABLE) {
                return TooltipBuilder.value("ENCHANTABLE");
            } else if (predicate == ItemFilter.ENCHANTED) {
                return TooltipBuilder.value("ENCHANTED");
            } else if (predicate == ItemFilter.BLOCK) {
                return TooltipBuilder.value("BLOCK");
            }

            List<ResourceLocationFilter.ByLocation> byLocation = PluginUtils.getCapturedInstances(predicate, ResourceLocationFilter.ByLocation.class);

            if (byLocation.size() == 1) {
                List<Integer> minMax = PluginUtils.getCapturedInstances(predicate, Integer.class);

                if (minMax.size() == 2) {
                    TooltipBuilder tooltip = TooltipBuilder.value("HAS_ENCHANTMENT");
                    int min = Math.min(minMax.get(0), minMax.get(1));
                    int max = Math.max(minMax.get(0), minMax.get(1));

                    tooltip.add(utils.getValueTooltip(utils, byLocation.get(0).location()).build(Lang.Value.ENCHANTMENT));

                    if (min != 1 && max != 255) {
                        tooltip.add(utils.getValueTooltip(utils, IntRange.range(min, max)).build(Lang.Value.LEVELS));
                    }

                    return tooltip;
                }
            }

            List<ResourceLocationFilter.ByPattern> byPattern = PluginUtils.getCapturedInstances(predicate, ResourceLocationFilter.ByPattern.class);

            if (byPattern.size() == 1) {
                List<Integer> minMax = PluginUtils.getCapturedInstances(predicate, Integer.class);

                if (minMax.size() == 2) {
                    TooltipBuilder tooltip = TooltipBuilder.value("HAS_ENCHANTMENT");
                    int min = Math.min(minMax.get(0), minMax.get(1));
                    int max = Math.max(minMax.get(0), minMax.get(1));

                    tooltip.add(utils.getValueTooltip(utils, byPattern.get(0).toString()).build(Lang.Value.ENCHANTMENT));

                    if (min != 1 && max != 255) {
                        tooltip.add(utils.getValueTooltip(utils, IntRange.range(min, max)).build(Lang.Value.LEVELS));
                    }

                    return tooltip;
                }
            }

            List<Ingredient> ingredient = PluginUtils.getCapturedInstances(predicate, Ingredient.class);

            if (ingredient.size() == 1) {
                Ingredient i = ingredient.get(0);

                if (!i.isEmpty()) {
                    return TooltipBuilder.value("INGREDIENT").add(utils.getValueTooltip(utils, i));
                }
            }
        }

        return TooltipBuilder.value("UNKNOWN");
    }

    @NotNull
    public static TooltipBuilder getStructureLocatorTooltip(IServerUtils utils, AnyStructure.StructureLocator structureLocator) {
        if (structureLocator instanceof AnyStructure.ById byId) {
            return utils.getValueTooltip(utils, byId.id());
        } else if (structureLocator instanceof AnyStructure.ByTag byTag) {
            return utils.getValueTooltip(utils, byTag.tag());
        }

        return TooltipBuilder.empty();
    }
}

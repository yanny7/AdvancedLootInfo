package com.yanny.ali.lootjs.server;

import com.almostreliable.lootjs.filters.ItemFilter;
import com.almostreliable.lootjs.filters.ResourceLocationFilter;
import com.almostreliable.lootjs.loot.condition.AnyStructure;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import com.yanny.ali.plugin.common.ReflectionUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
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
                return filterTooltip(Lang.ItemFilter.ALWAYS_FALSE);
            } else if (predicate == ItemFilter.ALWAYS_TRUE) {
                return filterTooltip(Lang.ItemFilter.ALWAYS_TRUE);
            } else if (predicate == ItemFilter.SWORD) {
                return filterTooltip(Lang.ItemFilter.SWORD);
            } else if (predicate == ItemFilter.PICKAXE) {
                return filterTooltip(Lang.ItemFilter.PICKAXE);
            } else if (predicate == ItemFilter.AXE) {
                return filterTooltip(Lang.ItemFilter.AXE);
            } else if (predicate == ItemFilter.SHOVEL) {
                return filterTooltip(Lang.ItemFilter.SHOVEL);
            } else if (predicate == ItemFilter.HOE) {
                return filterTooltip(Lang.ItemFilter.HOE);
            } else if (predicate == ItemFilter.TOOL) {
                return filterTooltip(Lang.ItemFilter.TOOL);
            } else if (predicate == ItemFilter.POTION) {
                return filterTooltip(Lang.ItemFilter.POTION);
            } else if (predicate == ItemFilter.HAS_TIER) {
                return filterTooltip(Lang.ItemFilter.HAS_TIER);
            } else if (predicate == ItemFilter.PROJECTILE_WEAPON) {
                return filterTooltip(Lang.ItemFilter.PROJECTILE_WEAPON);
            } else if (predicate == ItemFilter.ARMOR) {
                return filterTooltip(Lang.ItemFilter.ARMOR);
            } else if (predicate == ItemFilter.WEAPON) {
                return filterTooltip(Lang.ItemFilter.WEAPON);
            } else if (predicate == ItemFilter.HEAD_ARMOR) {
                return filterTooltip(Lang.ItemFilter.HEAD_ARMOR);
            } else if (predicate == ItemFilter.CHEST_ARMOR) {
                return filterTooltip(Lang.ItemFilter.CHEST_ARMOR);
            } else if (predicate == ItemFilter.LEGS_ARMOR) {
                return filterTooltip(Lang.ItemFilter.LEGS_ARMOR);
            } else if (predicate == ItemFilter.FEET_ARMOR) {
                return filterTooltip(Lang.ItemFilter.FEET_ARMOR);
            } else if (predicate == ItemFilter.FOOD) {
                return filterTooltip(Lang.ItemFilter.FOOD);
            } else if (predicate == ItemFilter.DAMAGEABLE) {
                return filterTooltip(Lang.ItemFilter.DAMAGEABLE);
            } else if (predicate == ItemFilter.DAMAGED) {
                return filterTooltip(Lang.ItemFilter.DAMAGED);
            } else if (predicate == ItemFilter.ENCHANTABLE) {
                return filterTooltip(Lang.ItemFilter.ENCHANTABLE);
            } else if (predicate == ItemFilter.ENCHANTED) {
                return filterTooltip(Lang.ItemFilter.ENCHANTED);
            } else if (predicate == ItemFilter.BLOCK) {
                return filterTooltip(Lang.ItemFilter.BLOCK);
            }

            List<ResourceLocationFilter.ByLocation> byLocation = ReflectionUtils.getCapturedInstances(predicate, ResourceLocationFilter.ByLocation.class);

            if (byLocation.size() == 1) {
                List<Integer> minMax = ReflectionUtils.getCapturedInstances(predicate, Integer.class);

                if (minMax.size() == 2) {
                    TooltipBuilder tooltip = filterTooltip(Lang.ItemFilter.HAS_ENCHANTMENT);
                    int min = Math.min(minMax.get(0), minMax.get(1));
                    int max = Math.max(minMax.get(0), minMax.get(1));

                    tooltip.add(utils.getValueTooltip(utils, ResourceKey.create(Registries.ENCHANTMENT, byLocation.get(0).location())).build(Lang.Value.ENCHANTMENT));

                    if (min != 1 || max != 255) {
                        tooltip.add(utils.getValueTooltip(utils, IntRange.range(min, max)).build(Lang.Value.LEVELS));
                    }

                    return tooltip;
                }
            }

            List<ResourceLocationFilter.ByPattern> byPattern = ReflectionUtils.getCapturedInstances(predicate, ResourceLocationFilter.ByPattern.class);

            if (byPattern.size() == 1) {
                List<Integer> minMax = ReflectionUtils.getCapturedInstances(predicate, Integer.class);

                if (minMax.size() == 2) {
                    TooltipBuilder tooltip = filterTooltip(Lang.ItemFilter.HAS_ENCHANTMENT);
                    int min = Math.min(minMax.get(0), minMax.get(1));
                    int max = Math.max(minMax.get(0), minMax.get(1));

                    tooltip.add(utils.getValueTooltip(utils, byPattern.get(0).pattern().pattern()).build(Lang.Value.ENCHANTMENT));

                    if (min != 1 || max != 255) {
                        tooltip.add(utils.getValueTooltip(utils, IntRange.range(min, max)).build(Lang.Value.LEVELS));
                    }

                    return tooltip;
                }
            }

            List<Ingredient> ingredient = ReflectionUtils.getCapturedInstances(predicate, Ingredient.class);

            if (ingredient.size() == 1) {
                Ingredient i = ingredient.get(0);

                if (!i.isEmpty()) {
                    return filterTooltip(Lang.ItemFilter.INGREDIENT).add(utils.getValueTooltip(utils, i));
                }
            }
        }

        return filterTooltip(Lang.ItemFilter.UNKNOWN);
    }

    @NotNull
    private static TooltipBuilder filterTooltip(Lang.ItemFilter filter) {
        return TooltipBuilder.value(TooltipBuilder.translate(filter.singular()));
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

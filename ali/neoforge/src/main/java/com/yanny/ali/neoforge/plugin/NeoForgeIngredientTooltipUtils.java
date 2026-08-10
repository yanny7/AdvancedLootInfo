package com.yanny.ali.neoforge.plugin;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.BlockTagIngredient;
import net.neoforged.neoforge.common.crafting.CompoundIngredient;
import net.neoforged.neoforge.common.crafting.DataComponentIngredient;
import net.neoforged.neoforge.common.crafting.DifferenceIngredient;
import net.neoforged.neoforge.common.crafting.IntersectionIngredient;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class NeoForgeIngredientTooltipUtils {
    @NotNull
    public static TooltipBuilder getCompoundIngredientTooltip(IServerUtils utils, CompoundIngredient ingredient) {
        return getChildrenTooltip(utils, ingredient.children()).key(Lang.Branch.ANY);
    }

    @NotNull
    public static TooltipBuilder getIntersectionIngredientTooltip(IServerUtils utils, IntersectionIngredient ingredient) {
        return getChildrenTooltip(utils, ingredient.children()).key(Lang.Branch.ALL);
    }

    @NotNull
    public static TooltipBuilder getDifferenceIngredientTooltip(IServerUtils utils, DifferenceIngredient ingredient) {
        return TooltipBuilder.array((b) -> b
                .add(utils.getValueTooltip(utils, ingredient.base()).build(Lang.Branch.BASE))
                .add(utils.getValueTooltip(utils, ingredient.subtracted()).build(Lang.Branch.SUBTRACTED))
        );
    }

    @NotNull
    public static TooltipBuilder getDataComponentIngredientTooltip(IServerUtils utils, DataComponentIngredient ingredient) {
        List<Holder<Item>> items = ingredient.items().toList();

        return TooltipBuilder.array((b) -> b
                .add(TooltipBuilder.array((c) -> items.forEach((i) -> c.add(TooltipBuilder.asElement(utils.getValueTooltip(utils, i.value()), items.size())))).build(Lang.Branch.ITEMS))
                .add(utils.getValueTooltip(utils, ingredient.components()).build(Lang.Branch.COMPONENTS))
                .add(utils.getValueTooltip(utils, ingredient.isStrict()).build(Lang.Value.EXACT))
        );
    }

    @NotNull
    public static TooltipBuilder getBlockTagIngredientTooltip(IServerUtils utils, BlockTagIngredient ingredient) {
        return utils.getValueTooltip(utils, ingredient.getTag()).key(Lang.Value.TAG);
    }

    @NotNull
    private static TooltipBuilder getChildrenTooltip(IServerUtils utils, List<Ingredient> children) {
        return TooltipBuilder.array((b) -> children.forEach((i) -> b.add(utils.getValueTooltip(utils, i))));
    }
}

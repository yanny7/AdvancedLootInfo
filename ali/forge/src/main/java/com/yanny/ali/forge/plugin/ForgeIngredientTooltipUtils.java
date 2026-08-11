package com.yanny.ali.forge.plugin;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.forge.mixin.MixinCompoundIngredient;
import com.yanny.ali.forge.mixin.MixinDifferenceIngredient;
import com.yanny.ali.forge.mixin.MixinIntersectionIngredient;
import com.yanny.ali.forge.mixin.MixinPartialNBTIngredient;
import com.yanny.ali.forge.mixin.MixinStrictNBTIngredient;
import com.yanny.ali.language.Lang;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.ingredients.CompoundIngredient;
import net.minecraftforge.common.crafting.ingredients.DifferenceIngredient;
import net.minecraftforge.common.crafting.ingredients.IntersectionIngredient;
import net.minecraftforge.common.crafting.ingredients.PartialNBTIngredient;
import net.minecraftforge.common.crafting.ingredients.StrictNBTIngredient;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ForgeIngredientTooltipUtils {
    @NotNull
    public static TooltipBuilder getCompoundIngredientTooltip(IServerUtils utils, CompoundIngredient ingredient) {
        return getChildrenTooltip(utils, ((MixinCompoundIngredient) ingredient).getChildren()).key(Lang.Branch.ANY);
    }

    @NotNull
    public static TooltipBuilder getIntersectionIngredientTooltip(IServerUtils utils, IntersectionIngredient ingredient) {
        return getChildrenTooltip(utils, ((MixinIntersectionIngredient) ingredient).getChildren()).key(Lang.Branch.ALL);
    }

    @NotNull
    public static TooltipBuilder getDifferenceIngredientTooltip(IServerUtils utils, DifferenceIngredient ingredient) {
        MixinDifferenceIngredient accessor = (MixinDifferenceIngredient) ingredient;

        return TooltipBuilder.array((b) -> b
                .add(utils.getValueTooltip(utils, accessor.getBase()).build(Lang.Branch.BASE))
                .add(utils.getValueTooltip(utils, accessor.getSubtracted()).build(Lang.Branch.SUBTRACTED))
        );
    }

    @NotNull
    public static TooltipBuilder getPartialNbtIngredientTooltip(IServerUtils utils, PartialNBTIngredient ingredient) {
        MixinPartialNBTIngredient accessor = (MixinPartialNBTIngredient) ingredient;
        List<Item> items = accessor.getItems();

        return TooltipBuilder.array((b) -> b
                .add(TooltipBuilder.array((c) -> items.forEach((i) -> c.add(TooltipBuilder.asElement(utils.getValueTooltip(utils, i), items.size())))).build(Lang.Branch.ITEMS))
                .add(utils.getValueTooltip(utils, accessor.getNbt()).build(Lang.Value.NBT))
        );
    }

    @NotNull
    public static TooltipBuilder getStrictNbtIngredientTooltip(IServerUtils utils, StrictNBTIngredient ingredient) {
        return utils.getValueTooltip(utils, ((MixinStrictNBTIngredient) ingredient).getStack());
    }

    @NotNull
    private static TooltipBuilder getChildrenTooltip(IServerUtils utils, List<Ingredient> children) {
        return TooltipBuilder.array((b) -> children.forEach((i) -> b.add(utils.getValueTooltip(utils, i))));
    }
}

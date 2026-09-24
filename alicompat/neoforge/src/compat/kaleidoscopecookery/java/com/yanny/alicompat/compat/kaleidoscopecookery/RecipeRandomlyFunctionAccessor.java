package com.yanny.alicompat.compat.kaleidoscopecookery;

import com.github.ysbbbbbb.kaleidoscopecookery.item.RecipeItem;
import com.github.ysbbbbbb.kaleidoscopecookery.loot.RecipeRandomlyFunction;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.IFunctionTooltip;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class RecipeRandomlyFunctionAccessor extends BaseAccessor<RecipeRandomlyFunction> implements IFunctionTooltip {
    @FieldAccessor
    private List<RecipeItem.RecipeRecord> possibleRecipes;

    public RecipeRandomlyFunctionAccessor(RecipeRandomlyFunction parent) {
        super(parent);
    }

    @NotNull
    @Override
    public TooltipBuilder getTooltip(IServerUtils utils) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, possibleRecipes).build(Lang.Branch.RECIPES));
            b.add(utils.getValueTooltip(utils, parent.predicates).build(Lang.Branch.PREDICATES));
            b.showEmpty();
        }, KaleidoscopeCookeryLang.Functions.RECIPE_RANDOMLY);
    }
}

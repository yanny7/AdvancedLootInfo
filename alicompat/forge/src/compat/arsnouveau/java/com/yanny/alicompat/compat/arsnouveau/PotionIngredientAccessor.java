package com.yanny.alicompat.compat.arsnouveau;

import com.hollingsworth.arsnouveau.api.recipe.PotionIngredient;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.IIngredientTooltip;
import org.jetbrains.annotations.NotNull;

public class PotionIngredientAccessor extends BaseAccessor<PotionIngredient> implements IIngredientTooltip {
    public PotionIngredientAccessor(PotionIngredient parent) {
        super(parent);
    }

    @NotNull
    @Override
    public TooltipBuilder getTooltip(IServerUtils utils) {
        return TooltipBuilder.array((b) -> b.add(utils.getValueTooltip(utils, parent.getStack()).build(Lang.Value.ITEM)), ArsNouveauLang.Ingredient.POTION);
    }
}

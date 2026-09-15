package com.yanny.alicompat.compat.mantle;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.IIngredientTooltip;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;
import slimeknights.mantle.recipe.ingredient.PotionDisplayIngredient;

import java.util.List;

public class PotionDisplayIngredientAccessor extends BaseAccessor<PotionDisplayIngredient> implements IIngredientTooltip {
    @FieldAccessor
    private List<Item> items;

    @FieldAccessor
    private TagKey<Item> tag;

    public PotionDisplayIngredientAccessor(PotionDisplayIngredient parent) {
        super(parent);
    }

    @NotNull
    @Override
    public TooltipBuilder getTooltip(IServerUtils utils) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, items).build(Lang.Branch.ITEMS));
            b.add(utils.getValueTooltip(utils, tag).build(Lang.Value.TAG));
        }, MantleLang.Ingredient.POTION_DISPLAY);
    }
}

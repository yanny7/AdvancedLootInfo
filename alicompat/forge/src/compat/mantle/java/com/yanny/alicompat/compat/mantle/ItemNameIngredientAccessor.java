package com.yanny.alicompat.compat.mantle;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.IIngredientTooltip;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import slimeknights.mantle.recipe.data.ItemNameIngredient;

import java.util.List;

public class ItemNameIngredientAccessor extends BaseAccessor<ItemNameIngredient> implements IIngredientTooltip {
    @FieldAccessor
    private List<ResourceLocation> names;

    public ItemNameIngredientAccessor(ItemNameIngredient parent) {
        super(parent);
    }

    @NotNull
    @Override
    public TooltipBuilder getTooltip(IServerUtils utils) {
        return TooltipBuilder.array((b) -> b.add(utils.getValueTooltip(utils, names.stream().map((name) -> ResourceKey.create(Registries.ITEM, name)).toList()).build(Lang.Branch.ITEMS)), MantleLang.Ingredient.ITEM_NAME);
    }
}

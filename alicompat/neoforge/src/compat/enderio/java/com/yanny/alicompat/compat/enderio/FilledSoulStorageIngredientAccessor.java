package com.yanny.alicompat.compat.enderio;

import com.enderio.enderio.api.soul.binding.ingredients.FilledSoulStorageIngredient;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.IValueTooltip;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;

public class FilledSoulStorageIngredientAccessor extends BaseAccessor<FilledSoulStorageIngredient> implements IValueTooltip {
    @FieldAccessor
    private Item item;

    public FilledSoulStorageIngredientAccessor(FilledSoulStorageIngredient parent) {
        super(parent);
    }

    @NotNull
    @Override
    public TooltipBuilder getTooltip(IServerUtils utils) {
        return TooltipBuilder.array((b) -> b.add(utils.getValueTooltip(utils, item).build(Lang.Value.ITEM)), EnderIoLang.Ingredient.FILLED_SOUL_STORAGE);
    }
}

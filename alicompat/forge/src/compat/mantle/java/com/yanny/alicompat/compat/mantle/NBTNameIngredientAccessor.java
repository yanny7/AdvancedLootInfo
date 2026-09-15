package com.yanny.alicompat.compat.mantle;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.IIngredientTooltip;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import slimeknights.mantle.recipe.data.NBTNameIngredient;

public class NBTNameIngredientAccessor extends BaseAccessor<NBTNameIngredient> implements IIngredientTooltip {
    @FieldAccessor
    private ResourceLocation name;

    @FieldAccessor
    private CompoundTag nbt;

    public NBTNameIngredientAccessor(NBTNameIngredient parent) {
        super(parent);
    }

    @NotNull
    @Override
    public TooltipBuilder getTooltip(IServerUtils utils) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, name).build(Lang.Value.ITEM));
            b.add(utils.getValueTooltip(utils, nbt).build(Lang.Value.NBT));
        }, MantleLang.Ingredient.NBT_NAME);
    }
}

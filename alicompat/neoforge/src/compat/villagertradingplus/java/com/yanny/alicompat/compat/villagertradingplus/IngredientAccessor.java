package com.yanny.alicompat.compat.villagertradingplus;

import com.lion.villagertradingplus.tradeoffers.util.Ingredient;
import com.mojang.datafixers.util.Either;
import com.yanny.aci.api.RangeValue;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.ReflectionUtils;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class IngredientAccessor extends BaseAccessor<Ingredient> {
    @Nullable
    @FieldAccessor
    private ItemStack fixed;

    @Nullable
    @FieldAccessor
    private TagKey<Item> tag;

    @FieldAccessor
    private int count;

    public IngredientAccessor(Ingredient parent) {
        super(parent);
    }

    @NotNull
    public static IngredientAccessor of(Ingredient ingredient) {
        return ReflectionUtils.copyClassData(IngredientAccessor.class, ingredient, Ingredient.class);
    }

    @NotNull
    public Either<ItemStack, TagKey<? extends ItemLike>> getItem() {
        if (tag != null) {
            return Either.right(tag);
        }

        return Either.left(fixed != null ? fixed : ItemStack.EMPTY);
    }

    @NotNull
    public RangeValue getCount() {
        if (tag != null) {
            return new RangeValue(count);
        }

        return new RangeValue(fixed != null ? fixed.getCount() : 1);
    }
}

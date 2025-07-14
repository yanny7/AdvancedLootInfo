package com.yanny.ali.mixin;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Ingredient.class)
public interface MixinIngredient {
    @Mixin(Ingredient.ItemValue.class)
    interface ItemValue {
        @Accessor
        ItemStack getItem();
    }

    @Mixin(Ingredient.TagValue.class)
    interface TagValue {
        @Accessor
        TagKey<Item> getTag();
    }
}

package com.yanny.ali.fabric.mixin;

import net.fabricmc.fabric.impl.recipe.ingredient.builtin.CustomDataIngredient;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.crafting.Ingredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CustomDataIngredient.class)
public interface MixinCustomDataIngredient {
    @Accessor(value = "base", remap = false)
    Ingredient getBase();

    @Accessor(value = "nbt", remap = false)
    CompoundTag getNbt();
}

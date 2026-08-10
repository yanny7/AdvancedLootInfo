package com.yanny.ali.forge.mixin;

import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.ingredients.DifferenceIngredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(DifferenceIngredient.class)
public interface MixinDifferenceIngredient {
    @Accessor
    Ingredient getBase();

    @Accessor
    Ingredient getSubtracted();
}

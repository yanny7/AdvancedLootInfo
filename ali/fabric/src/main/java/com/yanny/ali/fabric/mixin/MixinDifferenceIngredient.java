package com.yanny.ali.fabric.mixin;

import net.minecraft.world.item.crafting.Ingredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = {"net/fabricmc/fabric/impl/recipe/ingredient/builtin/DifferenceIngredient"})
public interface MixinDifferenceIngredient {
    @Accessor(value = "base", remap = false)
    Ingredient getBase();

    @Accessor(value = "subtracted", remap = false)
    Ingredient getSubtracted();
}

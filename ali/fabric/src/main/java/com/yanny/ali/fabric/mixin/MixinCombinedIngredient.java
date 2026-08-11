package com.yanny.ali.fabric.mixin;

import net.minecraft.world.item.crafting.Ingredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(targets = {"net/fabricmc/fabric/impl/recipe/ingredient/builtin/CombinedIngredient"})
public interface MixinCombinedIngredient {
    @Accessor(value = "ingredients", remap = false)
    List<Ingredient> getIngredients();
}

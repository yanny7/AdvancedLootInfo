package com.yanny.ali.fabric.mixin;

import net.fabricmc.fabric.impl.recipe.ingredient.builtin.ComponentsIngredient;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.item.crafting.Ingredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ComponentsIngredient.class)
public interface MixinComponentsIngredient {
    @Accessor(value = "base", remap = false)
    Ingredient getBase();

    @Accessor(value = "components", remap = false)
    DataComponentPatch getComponents();
}

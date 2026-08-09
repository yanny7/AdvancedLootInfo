package com.yanny.ali.forge.mixin;

import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.CompoundIngredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(CompoundIngredient.class)
public interface MixinCompoundIngredient {
    @Accessor
    List<Ingredient> getChildren();
}

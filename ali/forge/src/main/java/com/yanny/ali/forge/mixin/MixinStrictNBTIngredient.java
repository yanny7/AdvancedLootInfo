package com.yanny.ali.forge.mixin;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.crafting.ingredients.StrictNBTIngredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(StrictNBTIngredient.class)
public interface MixinStrictNBTIngredient {
    @Accessor
    ItemStack getStack();
}

package com.yanny.ali.forge.mixin;

import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.IntersectionIngredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(IntersectionIngredient.class)
public interface MixinIntersectionIngredient {
    @Accessor
    List<Ingredient> getChildren();
}

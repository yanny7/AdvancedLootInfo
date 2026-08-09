package com.yanny.ali.fabric.mixin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.crafting.Ingredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = {"net/fabricmc/fabric/impl/recipe/ingredient/builtin/NbtIngredient"})
public interface MixinNbtIngredient {
    @Accessor(value = "base", remap = false)
    Ingredient getBase();

    @Accessor(value = "nbt", remap = false)
    CompoundTag getNbt();

    @Accessor(value = "strict", remap = false)
    boolean isStrict();
}

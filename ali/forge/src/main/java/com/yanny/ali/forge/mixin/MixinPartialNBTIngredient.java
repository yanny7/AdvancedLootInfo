package com.yanny.ali.forge.mixin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.crafting.ingredients.PartialNBTIngredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(PartialNBTIngredient.class)
public interface MixinPartialNBTIngredient {
    @Accessor
    List<Item> getItems();

    @Accessor
    CompoundTag getNbt();
}

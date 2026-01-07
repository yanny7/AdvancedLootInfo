package com.yanny.ali.mixin;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.loot.AddTableLootModifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AddTableLootModifier.class)
public interface MixinAddTableLootModifier {
    @Accessor
    ResourceLocation getTable();
}

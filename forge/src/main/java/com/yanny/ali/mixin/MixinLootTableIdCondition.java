package com.yanny.ali.mixin;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.loot.LootTableIdCondition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = LootTableIdCondition.class, remap = false)
public interface MixinLootTableIdCondition {
    @Accessor
    ResourceLocation getTargetLootTableId();
}

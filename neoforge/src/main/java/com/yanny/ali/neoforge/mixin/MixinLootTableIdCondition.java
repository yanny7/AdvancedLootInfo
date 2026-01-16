package com.yanny.ali.neoforge.mixin;

import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.common.loot.LootTableIdCondition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LootTableIdCondition.class)
public interface MixinLootTableIdCondition {
    @Accessor
    Identifier getTargetLootTableId();
}

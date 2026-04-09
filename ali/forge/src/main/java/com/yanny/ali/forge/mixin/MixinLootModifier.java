package com.yanny.ali.forge.mixin;

import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.LootModifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LootModifier.class)
public interface MixinLootModifier {
    @Accessor
    LootItemCondition[] getConditions();
}

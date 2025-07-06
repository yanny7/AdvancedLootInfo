package com.yanny.ali.mixin;

import com.almostreliable.lootjs.loot.condition.LootItemConditionWrapper;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LootItemConditionWrapper.class)
public interface MixinLootItemConditionWrapper {
    @Accessor
    LootItemCondition getCondition();
}

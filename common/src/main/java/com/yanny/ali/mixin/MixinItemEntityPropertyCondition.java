package com.yanny.ali.mixin;

import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LootItemEntityPropertyCondition.class)
public interface MixinItemEntityPropertyCondition {
    @Accessor
    EntityPredicate getPredicate();

    @Accessor
    LootContext.EntityTarget getEntityTarget();
}

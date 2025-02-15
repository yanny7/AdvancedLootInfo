package com.yanny.ali.mixin;

import net.minecraft.advancements.critereon.DamageSourcePredicate;
import net.minecraft.world.level.storage.loot.predicates.DamageSourceCondition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(DamageSourceCondition.class)
public interface MixinDamageSourceCondition {
    @Accessor
    DamageSourcePredicate getPredicate();
}

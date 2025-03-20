package com.yanny.ali.mixin;

import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.TagPredicate;
import net.minecraft.world.damagesource.DamageType;

import java.util.List;

public interface MixinDamageSourcePredicate {
    List<TagPredicate<DamageType>> getTags();
    EntityPredicate getDirectEntity();
    EntityPredicate getSourceEntity();
}

package com.yanny.emi_loot_addon.mixin;

import net.minecraft.advancements.critereon.DamageSourcePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.TagPredicate;
import net.minecraft.world.damagesource.DamageType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(DamageSourcePredicate.class)
public interface MixinDamageSourcePredicate {
    @Accessor
    List<TagPredicate<DamageType>> getTags();

    @Accessor
    EntityPredicate getDirectEntity();

    @Accessor
    EntityPredicate getSourceEntity();
}

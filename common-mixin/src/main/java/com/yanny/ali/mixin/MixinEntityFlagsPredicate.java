package com.yanny.ali.mixin;

import net.minecraft.advancements.critereon.EntityFlagsPredicate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import javax.annotation.Nullable;

@Mixin(EntityFlagsPredicate.class)
public interface MixinEntityFlagsPredicate {
    @Nullable
    @Accessor
    Boolean getIsOnFire();

    @Nullable
    @Accessor
    Boolean getIsCrouching();

    @Nullable
    @Accessor
    Boolean getIsSprinting();

    @Nullable
    @Accessor
    Boolean getIsSwimming();

    @Nullable
    @Accessor
    Boolean getIsBaby();
}

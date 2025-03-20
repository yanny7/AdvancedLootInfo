package com.yanny.ali.mixin;

import javax.annotation.Nullable;

public interface MixinEntityFlagsPredicate {
    @Nullable
    Boolean getIsOnFire();

    @Nullable
    Boolean getIsCrouching();

    @Nullable
    Boolean getIsSprinting();

    @Nullable
    Boolean getIsSwimming();

    @Nullable
    Boolean getIsBaby();
}

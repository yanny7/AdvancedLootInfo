package com.yanny.ali.mixin;

import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface MixinStatePropertiesPredicate {
    interface PropertyMatcher {
        String getName();
    }

    interface ExactPropertyMatcher extends PropertyMatcher {
        String getValue();
    }

    interface RangedPropertyMatcher extends PropertyMatcher {
        @Nullable
        String getMinValue();
        @Nullable
        String getMaxValue();
    }

    List<StatePropertiesPredicate.PropertyMatcher> getProperties();
}

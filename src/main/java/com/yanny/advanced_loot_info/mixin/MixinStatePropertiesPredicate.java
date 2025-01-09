package com.yanny.advanced_loot_info.mixin;

import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(StatePropertiesPredicate.class)
public interface MixinStatePropertiesPredicate {
    @Mixin(StatePropertiesPredicate.PropertyMatcher.class)
    interface PropertyMatcher {
        @Accessor
        String getName();
    }

    @Mixin(StatePropertiesPredicate.ExactPropertyMatcher.class)
    interface ExactPropertyMatcher extends PropertyMatcher {
        @Accessor
        String getValue();
    }

    @Mixin(StatePropertiesPredicate.RangedPropertyMatcher.class)
    interface RangedPropertyMatcher extends PropertyMatcher {
        @Accessor
        String getMinValue();

        @Accessor
        String getMaxValue();
    }

    @Accessor
    List<StatePropertiesPredicate.PropertyMatcher> getProperties();
}

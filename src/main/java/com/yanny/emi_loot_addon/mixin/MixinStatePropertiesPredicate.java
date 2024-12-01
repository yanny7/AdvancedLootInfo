package com.yanny.emi_loot_addon.mixin;

import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(StatePropertiesPredicate.class)
public interface MixinStatePropertiesPredicate {
    @Accessor
    List<StatePropertiesPredicate.PropertyMatcher> getProperties();
}

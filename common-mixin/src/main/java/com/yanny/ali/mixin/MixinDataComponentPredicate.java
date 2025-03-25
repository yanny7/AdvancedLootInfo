package com.yanny.ali.mixin;

import net.minecraft.core.component.DataComponentPredicate;
import net.minecraft.core.component.TypedDataComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(DataComponentPredicate.class)
public interface MixinDataComponentPredicate {
    @Accessor
    List<TypedDataComponent<?>> getExpectedComponents();
}

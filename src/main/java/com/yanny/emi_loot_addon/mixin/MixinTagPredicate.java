package com.yanny.emi_loot_addon.mixin;

import net.minecraft.advancements.critereon.TagPredicate;
import net.minecraft.tags.TagKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(TagPredicate.class)
public interface MixinTagPredicate<T> {
    @Accessor
    TagKey<T> getTag();

    @Accessor
    boolean getExpected();
}

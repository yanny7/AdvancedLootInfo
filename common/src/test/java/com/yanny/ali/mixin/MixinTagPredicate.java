package com.yanny.ali.mixin;

import net.minecraft.tags.TagKey;

public interface MixinTagPredicate<T> {
    TagKey<T> getTag();
    boolean getExpected();
}

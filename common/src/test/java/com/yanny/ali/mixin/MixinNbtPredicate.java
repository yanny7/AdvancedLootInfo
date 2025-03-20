package com.yanny.ali.mixin;

import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;

public interface MixinNbtPredicate {
    @Nullable
    CompoundTag getTag();
}

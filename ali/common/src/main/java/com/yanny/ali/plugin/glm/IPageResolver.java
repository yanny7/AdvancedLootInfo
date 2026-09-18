package com.yanny.ali.plugin.glm;

import com.yanny.ali.api.IServerUtils;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface IPageResolver<T> {
    @Nullable
    Verdict test(IServerUtils utils, T value, LootPage page);
}

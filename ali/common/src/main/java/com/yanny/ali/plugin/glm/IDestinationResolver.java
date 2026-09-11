package com.yanny.ali.plugin.glm;

import com.yanny.ali.api.IServerUtils;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface IDestinationResolver<T> {
    @Nullable
    Destination resolve(IServerUtils utils, T value);
}

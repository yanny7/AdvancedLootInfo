package com.yanny.alicompat.accessor;

import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.plugin.glm.Destination;
import org.jetbrains.annotations.Nullable;

public interface IDestination {
    @Nullable
    Destination getDestination(IServerUtils utils);
}

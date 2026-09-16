package com.yanny.alicompat.accessor;

import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.plugin.glm.IPageLootModifier;

import java.util.Optional;

public interface IGlobalLootModifierAccessor {
    Optional<IPageLootModifier> getLootModifier(IServerUtils utils);
}

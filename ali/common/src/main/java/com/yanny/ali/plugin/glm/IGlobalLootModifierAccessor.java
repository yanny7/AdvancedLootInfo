package com.yanny.ali.plugin.glm;

import com.yanny.ali.api.ILootModifier;
import com.yanny.ali.api.IServerUtils;

import java.util.Optional;

public interface IGlobalLootModifierAccessor {
    Optional<ILootModifier<?>> getLootModifier(IServerUtils utils, ILootTableIdConditionPredicate predicate);
}

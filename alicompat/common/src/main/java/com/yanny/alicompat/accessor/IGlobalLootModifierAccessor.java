package com.yanny.alicompat.accessor;

import com.yanny.ali.api.ILootModifier;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.plugin.glm.ILootTableIdConditionPredicate;

import java.util.Optional;

public interface IGlobalLootModifierAccessor {
    Optional<ILootModifier<?>> getLootModifier(IServerUtils utils, ILootTableIdConditionPredicate predicate);
}

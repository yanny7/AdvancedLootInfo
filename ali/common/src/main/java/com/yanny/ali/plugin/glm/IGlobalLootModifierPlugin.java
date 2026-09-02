package com.yanny.ali.plugin.glm;

import com.yanny.ali.api.ILootModifier;
import com.yanny.ali.api.IPlugin;
import com.yanny.ali.api.IServerUtils;

import java.util.Optional;
import java.util.function.BiFunction;

public interface IGlobalLootModifierPlugin extends IPlugin {
    /**
     * @deprecated use {@link #registerGlobalLootModifier(IRegistry, ILootTableIdConditionPredicate)} instead
     */
    @Deprecated(forRemoval = true, since = "2.2.0")
    default void registerGlobalLootModifier(IRegistry registry) {}

    void registerGlobalLootModifier(IRegistry registry, ILootTableIdConditionPredicate predicate);

    interface IRegistry {
        <T> void registerGlobalLootModifier(Class<T> type, BiFunction<IServerUtils, T, Optional<ILootModifier<?>>> getter);
    }
}

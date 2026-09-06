package com.yanny.ali.plugin.glm;

import com.yanny.ali.api.ILootModifier;
import com.yanny.ali.api.IPlugin;
import com.yanny.ali.api.IServerUtils;

import java.util.Optional;
import java.util.function.BiFunction;

public interface IGlobalLootModifierPlugin extends IPlugin {
    void registerGlobalLootModifier(IRegistry registry);

    interface IRegistry {
        <T> void registerGlobalLootModifier(Class<T> type, BiFunction<IServerUtils, T, Optional<ILootModifier<?>>> getter);
    }
}

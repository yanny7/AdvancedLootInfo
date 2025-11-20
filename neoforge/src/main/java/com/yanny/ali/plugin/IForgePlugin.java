package com.yanny.ali.plugin;

import com.yanny.ali.api.ILootModifier;
import com.yanny.ali.api.IPlugin;
import com.yanny.ali.api.IServerUtils;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;

import java.util.Optional;
import java.util.function.BiFunction;

public interface IForgePlugin extends IPlugin {
    void registerGLM(IRegistry registry);

    interface IRegistry {
        <T extends IGlobalLootModifier> void registerGlobalLootModifier(Class<T> type, BiFunction<IServerUtils, T, Optional<ILootModifier<?>>> getter);
    }
}

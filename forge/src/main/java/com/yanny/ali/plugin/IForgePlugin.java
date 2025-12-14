package com.yanny.ali.plugin;

import com.yanny.ali.api.ILootModifier;
import com.yanny.ali.api.IPlugin;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.plugin.glm.IGlobalLootModifierPlugin;
import com.yanny.ali.plugin.glm.ILootTableIdConditionPredicate;
import net.minecraftforge.common.loot.IGlobalLootModifier;

import java.util.Optional;
import java.util.function.BiFunction;

public interface IForgePlugin extends IPlugin {
    void registerGlobalLootModifier(IGlobalLootModifierPlugin.IRegistry registry, ILootTableIdConditionPredicate predicate);

    interface IRegistry {
        <T extends IGlobalLootModifier> void registerGlobalLootModifier(Class<T> type, BiFunction<IServerUtils, T, Optional<ILootModifier<?>>> getter);
    }
}

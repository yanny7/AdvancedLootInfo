package com.yanny.ali.plugin.glm;

import com.yanny.aci.CommonLogUtils;
import com.yanny.ali.Utils;
import com.yanny.ali.api.ILootModifier;
import com.yanny.ali.api.IPlugin;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.platform.Services;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.*;
import java.util.function.BiFunction;

public final class GlobalLootModifierCollector {
    private static final Logger LOGGER = CommonLogUtils.getLogger(Utils.MOD_ID);

    @NotNull
    public static List<ILootModifier<?>> collect(IServerUtils utils, Collection<? extends IGlobalLootModifierWrapper> modifiers) {
        Map<Class<?>, BiFunction<IServerUtils, Object, Optional<ILootModifier<?>>>> glmMap = new HashMap<>();
        Set<Class<?>> missingGLM = new HashSet<>();
        List<ILootModifier<?>> lootModifiers = new ArrayList<>();
        IGlobalLootModifierPlugin.IRegistry glmRegistry = getRegistry(glmMap);

        for (IPlugin plugin : Services.getPlatform().getPlugins()) {
            if (plugin instanceof IGlobalLootModifierPlugin glmPlugin) {
                glmPlugin.registerGlobalLootModifier(glmRegistry);
            }
        }

        for (IGlobalLootModifierWrapper wrapper : modifiers) {
            Object globalLootModifier = wrapper.getLootModifier();

            try {
                BiFunction<IServerUtils, Object, Optional<ILootModifier<?>>> getter = glmMap.get(globalLootModifier.getClass());

                if (getter != null) {
                    Optional<ILootModifier<?>> lootModifier = getter.apply(utils, globalLootModifier);

                    if (lootModifier.isPresent()) {
                        lootModifiers.add(lootModifier.get());
                    } else {
                        LOGGER.warn("Unable to locate destination for GLM {}", wrapper.getName());
                    }
                } else {
                    Optional<ILootModifier<?>> modifier = GlobalLootModifierUtils.getMissingGlobalLootModifier(utils, wrapper);

                    missingGLM.add(globalLootModifier.getClass());

                    if (modifier.isPresent()) {
                        lootModifiers.add(modifier.get());
                    } else {
                        LOGGER.warn("Unable to locate destination for auto GLM {}", wrapper.getName());
                    }
                }
            } catch (Throwable e) {
                LOGGER.warn("Failed to add GLM with error {}", e.getMessage(), e);
            }
        }

        missingGLM.forEach((c) -> LOGGER.warn("Missing GLM for {}", c.getName()));

        return lootModifiers;
    }

    @NotNull
    private static IGlobalLootModifierPlugin.IRegistry getRegistry(Map<Class<?>, BiFunction<IServerUtils, Object, Optional<ILootModifier<?>>>> glmMap) {
        return new IGlobalLootModifierPlugin.IRegistry() {
            @Override
            public <T> void registerGlobalLootModifier(Class<T> type, BiFunction<IServerUtils, T, Optional<ILootModifier<?>>> getter) {
                //noinspection unchecked
                glmMap.put(type, (u, t) -> getter.apply(u, (T) t));
            }
        };
    }

    private GlobalLootModifierCollector() {}
}

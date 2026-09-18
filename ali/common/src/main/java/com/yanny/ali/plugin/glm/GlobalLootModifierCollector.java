package com.yanny.ali.plugin.glm;

import com.yanny.aci.CommonLogUtils;
import com.yanny.ali.Utils;
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
    public static List<IPageLootModifier> collect(IServerUtils utils, Collection<? extends IGlobalLootModifierWrapper> modifiers) {
        Map<Class<?>, BiFunction<IServerUtils, Object, Optional<IPageLootModifier>>> glmMap = new HashMap<>();
        Set<Class<?>> missingGLM = new HashSet<>();
        List<IPageLootModifier> lootModifiers = new ArrayList<>();
        IGlobalLootModifierPlugin.IRegistry glmRegistry = getRegistry(glmMap);

        for (IPlugin plugin : Services.getPlatform().getPlugins()) {
            if (plugin instanceof IGlobalLootModifierPlugin glmPlugin) {
                glmPlugin.registerGlobalLootModifier(glmRegistry);
            }
        }

        for (IGlobalLootModifierWrapper wrapper : modifiers) {
            Object globalLootModifier = wrapper.getLootModifier();

            try {
                BiFunction<IServerUtils, Object, Optional<IPageLootModifier>> getter = glmMap.get(globalLootModifier.getClass());

                if (getter != null) {
                    Optional<IPageLootModifier> lootModifier = getter.apply(utils, globalLootModifier);

                    if (lootModifier.isPresent()) {
                        lootModifiers.add(lootModifier.get());
                    } else {
                        LOGGER.warn("No loot modifier produced for GLM {}", wrapper.getName());
                    }
                } else {
                    Optional<IPageLootModifier> modifier = GlobalLootModifierUtils.getMissingGlobalLootModifier(utils, wrapper);

                    missingGLM.add(globalLootModifier.getClass());

                    if (modifier.isPresent()) {
                        lootModifiers.add(modifier.get());
                    } else {
                        LOGGER.warn("No loot modifier produced for auto GLM {}", wrapper.getName());
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
    private static IGlobalLootModifierPlugin.IRegistry getRegistry(Map<Class<?>, BiFunction<IServerUtils, Object, Optional<IPageLootModifier>>> glmMap) {
        return new IGlobalLootModifierPlugin.IRegistry() {
            @Override
            public <T> void registerGlobalLootModifier(Class<T> type, BiFunction<IServerUtils, T, Optional<IPageLootModifier>> getter) {
                //noinspection unchecked
                glmMap.put(type, (u, t) -> getter.apply(u, (T) t));
            }
        };
    }

    private GlobalLootModifierCollector() {}
}

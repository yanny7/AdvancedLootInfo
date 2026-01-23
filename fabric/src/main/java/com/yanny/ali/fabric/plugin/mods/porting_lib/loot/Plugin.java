package com.yanny.ali.fabric.plugin.mods.porting_lib.loot;

import com.mojang.logging.LogUtils;
import com.yanny.ali.api.*;
import com.yanny.ali.platform.Services;
import com.yanny.ali.plugin.glm.GlobalLootModifierUtils;
import com.yanny.ali.plugin.glm.IGlobalLootModifierPlugin;
import com.yanny.ali.plugin.glm.IGlobalLootModifierWrapper;
import com.yanny.ali.plugin.glm.ILootTableIdConditionPredicate;
import com.yanny.ali.plugin.mods.PluginUtils;
import com.yanny.ali.plugin.mods.ReflectionUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.*;
import java.util.function.BiFunction;

@AliEntrypoint
public class Plugin implements IPlugin {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static Class<?> LOOT_MODIFIER_CLASS;
    private static Class<?> LOOT_TABLE_ID_CONDITION_CLASS;

    static {
        try {
            LOOT_MODIFIER_CLASS = Class.forName("io.github.fabricators_of_create.porting_lib.loot.LootModifier");
            LOOT_TABLE_ID_CONDITION_CLASS = Class.forName("io.github.fabricators_of_create.porting_lib.loot.LootTableIdCondition");
        } catch (ClassNotFoundException e) {
            LOGGER.warn("Unable to obtain GLM classes: {}", e.getMessage(), e);
        }
    }

    @Override
    public String getModId() {
        return "porting_lib_loot";
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        PluginUtils.registerConditionTooltip(registry, LootTableIdCondition.class);

        registry.registerLootModifiers(Plugin::registerLootModifiers);
    }

    @NotNull
    private static List<ILootModifier<?>> registerLootModifiers(IServerUtils utils) {
        Map<Class<?>, BiFunction<IServerUtils, Object, Optional<ILootModifier<?>>>> glmMap = new HashMap<>();
        Set<Class<?>> missingGLM = new HashSet<>();
        List<ILootModifier<?>> lootModifiers = new ArrayList<>();
        ILootTableIdConditionPredicate tablePredicate = getLootTableIdConditionPredicate();
        IGlobalLootModifierPlugin.IRegistry forgeRegistry = getRegistry(glmMap);

        for (IPlugin plugin : Services.getPlatform().getPlugins()) {
            if (plugin instanceof IGlmPlugin glmPlugin) {
                glmPlugin.registerGlobalLootModifier(forgeRegistry, tablePredicate);
            }
        }

        for (Map.Entry<ResourceLocation, Object> entry : LootModifierManager.getAllLootMods().entrySet()) {
            ResourceLocation location = entry.getKey();
            Object globalLootModifier = entry.getValue();
            IGlobalLootModifierWrapper wrapper = wrap(globalLootModifier, location);

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
                    Optional<ILootModifier<?>> modifier = GlobalLootModifierUtils.getMissingGlobalLootModifier(utils, wrapper, tablePredicate);

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
    private static ILootTableIdConditionPredicate getLootTableIdConditionPredicate() {
        return new ILootTableIdConditionPredicate() {
            @Override
            public boolean isLootTableIdCondition(LootItemCondition condition) {
                return LOOT_TABLE_ID_CONDITION_CLASS.isAssignableFrom(condition.getClass());
            }

            @Override
            public ResourceLocation getTargetLootTableId(LootItemCondition condition) {
                return ReflectionUtils.copyClassData(LootTableIdCondition.class, condition).getTargetLootTableId();
            }
        };
    }

    @NotNull
    private static IGlobalLootModifierWrapper wrap(Object modifier, ResourceLocation location) {
        return new IGlobalLootModifierWrapper() {
            @Override
            public ResourceLocation getName() {
                return location;
            }

            @Override
            public Class<?> getLootModifierClass() {
                return LOOT_MODIFIER_CLASS;
            }

            @Override
            public boolean isLootModifier() {
                return LOOT_MODIFIER_CLASS.isAssignableFrom(modifier.getClass());
            }

            @Override
            public List<LootItemCondition> getConditions() {
                return Arrays.asList(((LootModifier) modifier).getConditions());
            }
        };
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
}

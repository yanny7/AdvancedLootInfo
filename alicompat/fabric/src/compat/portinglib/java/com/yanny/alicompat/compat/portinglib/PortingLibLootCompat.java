package com.yanny.alicompat.compat.portinglib;

import com.google.gson.JsonElement;
import com.yanny.aci.CommonLogUtils;
import com.yanny.ali.api.ILootModifier;
import com.yanny.ali.api.IPlugin;
import com.yanny.ali.api.IServerRegistry;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.platform.Services;
import com.yanny.ali.plugin.glm.GlobalLootModifierUtils;
import com.yanny.ali.plugin.glm.IGlobalLootModifierPlugin;
import com.yanny.ali.plugin.glm.IGlobalLootModifierWrapper;
import com.yanny.ali.plugin.glm.ILootTableIdConditionPredicate;
import com.yanny.ali.plugin.mods.ReflectionUtils;
import com.yanny.alicompat.IModCompat;
import com.yanny.alicompat.Utils;
import io.github.fabricators_of_create.porting_lib.loot.IGlobalLootModifier;
import io.github.fabricators_of_create.porting_lib.loot.LootModifier;
import io.github.fabricators_of_create.porting_lib.loot.LootTableIdCondition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;

public class PortingLibLootCompat implements IModCompat {
    private static final Logger LOGGER = CommonLogUtils.getLogger(Utils.MOD_ID);

    @NotNull
    @Override
    public String targetModId() {
        return "porting_lib_loot";
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        registry.registerConditionTooltip(LootTableIdCondition.class, (utils, condition) ->
                ReflectionUtils.copyClassData(LootTableIdConditionAccessor.class, condition, LootTableIdCondition.class).getTooltip(utils));

        registry.registerLootModifiers(PortingLibLootCompat::registerLootModifiers);
    }

    @NotNull
    private static List<ILootModifier<?>> registerLootModifiers(IServerUtils utils) {
        Map<Class<?>, BiFunction<IServerUtils, Object, Optional<ILootModifier<?>>>> glmMap = new HashMap<>();
        Set<Class<?>> missingGLM = new HashSet<>();
        List<ILootModifier<?>> lootModifiers = new ArrayList<>();
        ILootTableIdConditionPredicate tablePredicate = getLootTableIdConditionPredicate();
        IGlobalLootModifierPlugin.IRegistry glmRegistry = getRegistry(glmMap);

        for (IPlugin plugin : Services.getPlatform().getPlugins()) {
            if (plugin instanceof IGlobalLootModifierPlugin glmPlugin) {
                glmPlugin.registerGlobalLootModifier(glmRegistry, tablePredicate);
            }
        }

        for (Map.Entry<ResourceLocation, IGlobalLootModifier> entry : LootModifierManagerAccessor.getAllLootMods().entrySet()) {
            ResourceLocation location = entry.getKey();
            IGlobalLootModifier globalLootModifier = entry.getValue();
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
                return condition instanceof LootTableIdCondition;
            }

            @Override
            public ResourceLocation getTargetLootTableId(LootItemCondition condition) {
                return ReflectionUtils.copyClassData(LootTableIdConditionAccessor.class, condition, LootTableIdCondition.class).getTargetLootTableId();
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

    @NotNull
    private static IGlobalLootModifierWrapper wrap(IGlobalLootModifier modifier, ResourceLocation location) {
        return new IGlobalLootModifierWrapper() {
            @Override
            public ResourceLocation getName() {
                return location;
            }

            @Override
            public Class<?> getLootModifierClass() {
                return LootModifier.class;
            }

            @Override
            public boolean isLootModifier() {
                return modifier instanceof LootModifier;
            }

            @Override
            public List<LootItemCondition> getConditions() {
                return Arrays.asList(ReflectionUtils.copyClassData(LootModifierAccessor.class, modifier, LootModifier.class).getConditions());
            }

            @Override
            public JsonElement serialize() {
                throw new IllegalStateException("Not implemented");
            }
        };
    }
}

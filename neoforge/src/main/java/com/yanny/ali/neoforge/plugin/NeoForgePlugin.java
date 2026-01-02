package com.yanny.ali.neoforge.plugin;

import com.mojang.logging.LogUtils;
import com.yanny.ali.api.*;
import com.yanny.ali.neoforge.mixin.MixinCanToolPerformAction;
import com.yanny.ali.neoforge.mixin.MixinLootModifier;
import com.yanny.ali.neoforge.mixin.MixinLootTableIdCondition;
import com.yanny.ali.neoforge.mixin.MixinNeoForgeEventHandler;
import com.yanny.ali.platform.Services;
import com.yanny.ali.plugin.glm.GlobalLootModifierUtils;
import com.yanny.ali.plugin.glm.IGlobalLootModifierPlugin;
import com.yanny.ali.plugin.glm.IGlobalLootModifierWrapper;
import com.yanny.ali.plugin.glm.ILootTableIdConditionPredicate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.*;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.slf4j.Logger;

import java.util.*;
import java.util.function.BiFunction;

@AliEntrypoint
public class NeoForgePlugin implements IPlugin {
    private static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public String getModId() {
        return "neoforge";
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        registry.registerConditionTooltip(CanToolPerformAction.class, NeoForgePlugin::getCanToolPerformActionTooltip);
        registry.registerConditionTooltip(LootTableIdCondition.class, NeoForgePlugin::getLootTableIdTooltip);

        registry.registerLootModifiers(NeoForgePlugin::registerLootModifiers);
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getCanToolPerformActionTooltip(IServerUtils utils, CanToolPerformAction condition) {
        MixinCanToolPerformAction cond = (MixinCanToolPerformAction) condition;
        return utils.getValueTooltip(utils, cond.getAction().name()).build("ali.type.condition.can_tool_perform_action");
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getLootTableIdTooltip(IServerUtils utils, LootTableIdCondition condition) {
        MixinLootTableIdCondition cond = (MixinLootTableIdCondition) condition;
        return utils.getValueTooltip(utils, cond.getTargetLootTableId()).build("ali.type.condition.loot_table_id");
    }

    @NotNull
    private static List<ILootModifier<?>> registerLootModifiers(IServerUtils utils) {
        Map<Class<?>, BiFunction<IServerUtils, IGlobalLootModifier, Optional<ILootModifier<?>>>> glmMap = new HashMap<>();
        Set<Class<?>> missingGLM = new HashSet<>();
        List<ILootModifier<?>> lootModifiers = new ArrayList<>();
        ILootTableIdConditionPredicate tablePredicate = getLootTableIdConditionPredicate();
        IGlobalLootModifierPlugin.IRegistry forgeRegistry = getForgeRegistry(glmMap);

        for (IPlugin plugin : Services.getPlatform().getPlugins()) {
            if (plugin instanceof IForgePlugin forgePlugin) {
                forgePlugin.registerGlobalLootModifier(forgeRegistry, tablePredicate);
            }
        }

        LootModifierManager lootModifierManager = MixinNeoForgeEventHandler.getLootModifierManager();

        for (IGlobalLootModifier globalLootModifier : lootModifierManager.getAllLootMods()) {
            IGlobalLootModifierWrapper wrapper = wrap(globalLootModifier);

            try {
                BiFunction<IServerUtils, IGlobalLootModifier, Optional<ILootModifier<?>>> getter = glmMap.get(globalLootModifier.getClass());

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
                e.printStackTrace();
                LOGGER.warn("Failed to add GLM with error {}", e.getMessage());
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
                return ((MixinLootTableIdCondition) condition).getTargetLootTableId();
            }
        };
    }

    @NotNull
    private static IGlobalLootModifierWrapper wrap(IGlobalLootModifier modifier) {
        return new IGlobalLootModifierWrapper() {
            @Override
            public ResourceLocation getName() {
                return NeoForgeRegistries.GLOBAL_LOOT_MODIFIER_SERIALIZERS.getKey(modifier.codec());
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
                return Arrays.asList(((MixinLootModifier) modifier).getConditions());
            }
        };
    }

    @NotNull
    private static IGlobalLootModifierPlugin.IRegistry getForgeRegistry(Map<Class<?>, BiFunction<IServerUtils, IGlobalLootModifier, Optional<ILootModifier<?>>>> glmMap) {
        return new IGlobalLootModifierPlugin.IRegistry() {
            @Override
            public <T> void registerGlobalLootModifier(Class<T> type, BiFunction<IServerUtils, T, Optional<ILootModifier<?>>> getter) {
                //noinspection unchecked
                glmMap.put(type, (u, t) -> getter.apply(u, (T) t));
            }
        };
    }
}

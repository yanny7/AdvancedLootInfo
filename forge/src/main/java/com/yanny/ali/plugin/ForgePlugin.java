package com.yanny.ali.plugin;

import com.mojang.logging.LogUtils;
import com.yanny.ali.api.*;
import com.yanny.ali.mixin.MixinForgeInternalHandler;
import com.yanny.ali.platform.Services;
import com.yanny.ali.plugin.client.widget.GlobalLootModifierWidget;
import com.yanny.ali.plugin.common.nodes.GlobalLootModifierNode;
import net.minecraftforge.common.loot.CanToolPerformAction;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifierManager;
import net.minecraftforge.common.loot.LootTableIdCondition;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.*;
import java.util.function.BiFunction;

@AliEntrypoint
public class ForgePlugin implements IPlugin {
    private static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public String getModId() {
        return "forge";
    }

    @Override
    public void registerClient(IClientRegistry registry) {
        registry.registerWidget(GlobalLootModifierNode.ID, GlobalLootModifierWidget::new);

        registry.registerDataNode(GlobalLootModifierNode.ID, GlobalLootModifierNode::new);
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        registry.registerConditionTooltip(CanToolPerformAction.class, ForgePlugin::getCanToolPerformActionTooltip);
        registry.registerConditionTooltip(LootTableIdCondition.class, ForgePlugin::getLootTableIdTooltip);

        registry.registerLootModifiers(ForgePlugin::registerLootModifiers);
    }

    @NotNull
    public static ITooltipNode getCanToolPerformActionTooltip(IServerUtils utils, CanToolPerformAction cond) {
        return utils.getValueTooltip(utils, cond.action.name()).build("ali.type.condition.can_tool_perform_action");
    }

    @NotNull
    public static ITooltipNode getLootTableIdTooltip(IServerUtils utils, LootTableIdCondition cond) {
        return utils.getValueTooltip(utils, cond.id()).build("ali.type.condition.loot_table_id");
    }

    @NotNull
    private static List<ILootModifier<?>> registerLootModifiers(IServerUtils utils) {
        Map<Class<?>, BiFunction<IServerUtils, IGlobalLootModifier, Optional<ILootModifier<?>>>> glmMap = new HashMap<>();
        Set<Class<?>> missingGLM = new HashSet<>();
        List<ILootModifier<?>> lootModifiers = new ArrayList<>();
        IForgePlugin.IRegistry forgeRegistry = new IForgePlugin.IRegistry() {
            @Override
            public <T extends IGlobalLootModifier> void registerGlobalLootModifier(Class<T> type, BiFunction<IServerUtils, T, Optional<ILootModifier<?>>> getter) {
                //noinspection unchecked
                glmMap.put(type, (u, t) -> getter.apply(u, (T) t));
            }
        };

        for (IPlugin plugin : Services.getPlatform().getPlugins()) {
            if (plugin instanceof IForgePlugin forgePlugin) {
                forgePlugin.registerGLM(forgeRegistry);
            }
        }

        LootModifierManager lootModifierManager = MixinForgeInternalHandler.getLootModifierManager();

        for (IGlobalLootModifier globalLootModifier : lootModifierManager.getAllLootMods()) {
            try {
                BiFunction<IServerUtils, IGlobalLootModifier, Optional<ILootModifier<?>>> getter = glmMap.get(globalLootModifier.getClass());

                if (getter != null) {
                    Optional<ILootModifier<?>> lootModifier = getter.apply(utils, globalLootModifier);

                    if (lootModifier.isPresent()) {
                        lootModifiers.add(lootModifier.get());
                    } else {
                        LOGGER.warn("Unable to locate destination for GLM {}", GlobalLootModifierUtils.getName(globalLootModifier));
                    }
                } else {
                    Optional<ILootModifier<?>> modifier = GlobalLootModifierUtils.getMissingGlobalLootModifier(utils, globalLootModifier);

                    missingGLM.add(globalLootModifier.getClass());

                    if (modifier.isPresent()) {
                        lootModifiers.add(modifier.get());
                    } else {
                        LOGGER.warn("Unable to locate destination for auto GLM {}", GlobalLootModifierUtils.getName(globalLootModifier));
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
}

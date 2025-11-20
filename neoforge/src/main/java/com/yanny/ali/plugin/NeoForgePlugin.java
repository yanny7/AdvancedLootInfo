package com.yanny.ali.plugin;

import com.mojang.logging.LogUtils;
import com.yanny.ali.api.*;
import com.yanny.ali.mixin.MixinCanItemPerformAbility;
import com.yanny.ali.mixin.MixinLootTableIdCondition;
import com.yanny.ali.mixin.MixinNeoForgeEventHandler;
import com.yanny.ali.platform.Services;
import com.yanny.ali.plugin.client.widget.GlobalLootModifierWidget;
import com.yanny.ali.plugin.client.widget.SingletonWidget;
import com.yanny.ali.plugin.common.nodes.GlobalLootModifierNode;
import com.yanny.ali.plugin.common.nodes.SingletonNode;
import net.neoforged.neoforge.common.loot.CanToolPerformAction;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifierManager;
import net.neoforged.neoforge.common.loot.LootTableIdCondition;
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
    public void registerClient(IClientRegistry registry) {
        registry.registerWidget(SingletonNode.ID, SingletonWidget::new);
        registry.registerWidget(GlobalLootModifierNode.ID, GlobalLootModifierWidget::new);

        registry.registerDataNode(SingletonNode.ID, SingletonNode::new);
        registry.registerDataNode(GlobalLootModifierNode.ID, GlobalLootModifierNode::new);
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        registry.registerConditionTooltip(CanItemPerformAbility.class, NeoForgePlugin::getCanToolPerformActionTooltip);
        registry.registerConditionTooltip(LootTableIdCondition.class, NeoForgePlugin::getLootTableIdTooltip);

        registry.registerLootModifiers(NeoForgePlugin::registerLootModifiers);
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getCanToolPerformActionTooltip(IServerUtils utils, CanItemPerformAbility condition) {
        MixinCanItemPerformAbility cond = (MixinCanItemPerformAbility) condition;
        return utils.getValueTooltip(utils, cond.getAbility()).build("ali.type.condition.can_item_perform_ability");
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getLootTableIdTooltip(IServerUtils utils, LootTableIdCondition condition) {
        MixinLootTableIdCondition cond = (MixinLootTableIdCondition) condition;
        return utils.getValueTooltip(utils, cond.getTargetLootTableId()).build("ali.type.condition.loot_table_id");
    }

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

        LootModifierManager lootModifierManager = MixinNeoForgeEventHandler.getLootModifierManager();

        for (IGlobalLootModifier globalLootModifier : lootModifierManager.getAllLootMods()) {
            try {
                BiFunction<IServerUtils, IGlobalLootModifier, Optional<ILootModifier<?>>> getter = glmMap.get(globalLootModifier.getClass());

                if (getter != null) {
                    Optional<ILootModifier<?>> lootModifier = getter.apply(utils, globalLootModifier);

                    if (lootModifier.isPresent()) {
                        lootModifiers.add(lootModifier.get());
                    } else {
                        LOGGER.warn("Unable to locate destination for GLM {}", globalLootModifier.getClass().getName());
                    }
                } else {
                    missingGLM.add(globalLootModifier.getClass());
                    GlobalLootModifierUtils.getMissingGlobalLootModifier(utils, globalLootModifier).ifPresent(lootModifiers::add);
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

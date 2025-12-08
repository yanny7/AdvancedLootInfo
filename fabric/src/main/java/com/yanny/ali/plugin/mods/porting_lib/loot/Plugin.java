package com.yanny.ali.plugin.mods.porting_lib.loot;

import com.mojang.logging.LogUtils;
import com.yanny.ali.api.*;
import com.yanny.ali.platform.Services;
import com.yanny.ali.plugin.mods.PluginUtils;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.*;
import java.util.function.BiFunction;

@AliEntrypoint
public class Plugin implements IPlugin {
    private static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public String getModId() {
        return "porting_lib_loot";
    }

    @Override
    public void registerClient(IClientRegistry registry) {
        registry.registerWidget(GlobalLootModifierNode.ID, GlobalLootModifierWidget::new);

        registry.registerDataNode(GlobalLootModifierNode.ID, GlobalLootModifierNode::new);
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
        IGlobalLootModifierPlugin.IRegistry forgeRegistry = new IGlobalLootModifierPlugin.IRegistry() {
            @Override
            public <T> void registerGlobalLootModifier(Class<T> type, BiFunction<IServerUtils, T, Optional<ILootModifier<?>>> getter) {
                //noinspection unchecked
                glmMap.put(type, (u, t) -> getter.apply(u, (T) t));
            }
        };

        for (IPlugin plugin : Services.getPlatform().getPlugins()) {
            if (plugin instanceof IGlobalLootModifierPlugin forgePlugin) {
                forgePlugin.registerGLM(forgeRegistry);
            }
        }

        for (Map.Entry<ResourceLocation, Object> entry : LootModifierManager.getAllLootMods().entrySet()) {
            Object globalLootModifier = entry.getValue();
            ResourceLocation location = entry.getKey();

            try {
                BiFunction<IServerUtils, Object, Optional<ILootModifier<?>>> getter = glmMap.get(globalLootModifier.getClass());

                if (getter != null) {
                    Optional<ILootModifier<?>> lootModifier = getter.apply(utils, globalLootModifier);

                    if (lootModifier.isPresent()) {
                        lootModifiers.add(lootModifier.get());
                    } else {
                        LOGGER.warn("Unable to locate destination for GLM {}", location);
                    }
                } else {
                    Optional<ILootModifier<?>> modifier = GlobalLootModifierUtils.getMissingGlobalLootModifier(utils, globalLootModifier, location);

                    missingGLM.add(globalLootModifier.getClass());

                    if (modifier.isPresent()) {
                        lootModifiers.add(modifier.get());
                    } else {
                        LOGGER.warn("Unable to locate destination for auto GLM {}", location);
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

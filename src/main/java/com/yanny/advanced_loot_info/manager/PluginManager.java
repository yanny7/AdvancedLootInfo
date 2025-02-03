package com.yanny.advanced_loot_info.manager;

import com.mojang.logging.LogUtils;
import com.yanny.advanced_loot_info.AdvancedLootInfoMod;
import com.yanny.advanced_loot_info.api.AliEntrypoint;
import com.yanny.advanced_loot_info.api.IPlugin;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.forgespi.language.ModFileScanData;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Type;
import org.slf4j.Logger;

import java.util.LinkedList;
import java.util.List;

public class PluginManager {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static AliRegistry REGISTRY;
    private static List<PluginHolder> PLUGINS;

    public static void registerCommonEvent(@SuppressWarnings("unused") FMLCommonSetupEvent event) {
        PLUGINS = getPlugins();
        registerCommonData();
    }

    public static void registerClientEvent(@SuppressWarnings("unused") FMLClientSetupEvent event) {
        registerClientData();
    }

    private static void registerCommonData() {
        REGISTRY = new AliRegistry();
        LOGGER.info("Registering common plugin data...");

        for (PluginHolder plugin : PLUGINS) {
            try {
                plugin.plugin().registerCommon(REGISTRY);
            } catch (Throwable throwable) {
                LOGGER.error("Failed to register {} common part with error: {}", plugin.modId(), throwable.getMessage());
            }
        }

        REGISTRY.printCommonInfo();
        LOGGER.info("Registering common plugin data finished");
    }

    private static void registerClientData() {
        LOGGER.info("Registering client plugin data...");

        for (PluginHolder plugin : PLUGINS) {
            try {
                plugin.plugin().registerClient(REGISTRY);
            } catch (Throwable throwable) {
                LOGGER.error("Failed to register {} client part with error: {}", plugin.modId(), throwable.getMessage());
            }
        }

        REGISTRY.printClientInfo();
        LOGGER.info("Registering client plugin data finished");
    }

    @NotNull
    private static List<PluginHolder> getPlugins() {
        List<PluginHolder> plugins = new LinkedList<>();
        Type type = Type.getType(AliEntrypoint.class);

        for (ModFileScanData scanData : ModList.get().getAllScanData()) {
            for (ModFileScanData.AnnotationData annotationData : scanData.getAnnotations()) {
                if (type.equals(annotationData.annotationType())) {
                    try {
                        Class<?> clazz = Class.forName(annotationData.memberName());

                        if (IPlugin.class.isAssignableFrom(clazz)) {
                            Class<? extends IPlugin> pluginClass = clazz.asSubclass(IPlugin.class);
                            IPlugin plugin = pluginClass.getConstructor().newInstance();
                            String modId = scanData.getIModInfoData().get(0).getMods().get(0).getModId();

                            plugins.add(new PluginHolder(modId, plugin));
                        } else {
                            LOGGER.warn("{} doesn't implement {}", annotationData.memberName(), IPlugin.class.getName());
                        }
                    } catch (Throwable t) {
                        LOGGER.warn("Failed to load {} plugin with error: {}", AdvancedLootInfoMod.MOD_ID, t.getMessage());
                    }
                }
            }
        }

        LOGGER.info("Found {} plugin(s", plugins.size());
        return plugins;
    }
}

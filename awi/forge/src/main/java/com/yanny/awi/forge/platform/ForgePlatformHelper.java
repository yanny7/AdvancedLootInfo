package com.yanny.awi.forge.platform;

import com.google.common.base.Suppliers;
import com.mojang.logging.LogUtils;
import com.yanny.awi.api.AwiEntrypoint;
import com.yanny.awi.api.IPlugin;
import com.yanny.awi.platform.services.IPlatformHelper;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.forgespi.language.ModFileScanData;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Type;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

public class ForgePlatformHelper implements IPlatformHelper {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final Supplier<List<IPlugin>> pluginsSupplier = Suppliers.memoize(this::loadPlugins);

    @Override
    public List<IPlugin> getPlugins() {
        return pluginsSupplier.get();
    }

    @Override
    public Path getConfiguration() {
        return FMLPaths.CONFIGDIR.get();
    }

    @NotNull
    private List<IPlugin> loadPlugins() {
        List<IPlugin> plugins = new LinkedList<>();
        Type type = Type.getType(AwiEntrypoint.class);

        for (ModFileScanData scanData : ModList.get().getAllScanData()) {
            for (ModFileScanData.AnnotationData annotationData : scanData.getAnnotations()) {
                if (type.equals(annotationData.annotationType())) {
                    try {
                        Class<?> clazz = Class.forName(annotationData.memberName());

                        if (IPlugin.class.isAssignableFrom(clazz)) {
                            Class<? extends IPlugin> pluginClass = clazz.asSubclass(IPlugin.class);
                            IPlugin plugin = pluginClass.getConstructor().newInstance();

                            if (ModList.get().isLoaded(plugin.getModId())) {
                                plugins.add(plugin);
                                LOGGER.info("Registered AWI plugin [{}] {}", plugin.getModId(), plugin.getClass().getCanonicalName());
                            }
                        } else {
                            LOGGER.warn("{} doesn't implement {}", annotationData.memberName(), IPlugin.class.getName());
                        }
                    } catch (Throwable e) {
                        LOGGER.warn("Failed to load plugin with error: {}", e.getMessage(), e);
                    }
                }
            }
        }

        LOGGER.info("Found {} plugin(s)", plugins.size());
        return plugins;
    }
}

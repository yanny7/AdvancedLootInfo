package com.yanny.ali.platform;

import com.mojang.logging.LogUtils;
import com.yanny.ali.AliMod;
import com.yanny.ali.api.AliEntrypoint;
import com.yanny.ali.api.IPlugin;
import com.yanny.ali.manager.PluginHolder;
import com.yanny.ali.network.AbstractClient;
import com.yanny.ali.network.AbstractServer;
import com.yanny.ali.network.DistHolder;
import com.yanny.ali.platform.services.IPlatformHelper;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.ModFileScanData;
import org.objectweb.asm.Type;
import org.slf4j.Logger;

import java.util.LinkedList;
import java.util.List;

public class ForgePlatformHelper implements IPlatformHelper {
    private static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public List<PluginHolder> getPlugins() {
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
                        LOGGER.warn("Failed to load plugin with error: {}", t.getMessage());
                    }
                }
            }
        }

        LOGGER.info("Found {} plugin(s", plugins.size());
        return plugins;
    }

    @Override
    public DistHolder<AbstractClient, AbstractServer> getInfoPropagator() {
        return AliMod.INFO_PROPAGATOR;
    }
}

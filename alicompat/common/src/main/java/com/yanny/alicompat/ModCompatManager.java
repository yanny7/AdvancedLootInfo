package com.yanny.alicompat;

import com.yanny.aci.CommonLogUtils;
import com.yanny.ali.api.IClientRegistry;
import com.yanny.ali.api.ICommonRegistry;
import com.yanny.ali.api.IServerRegistry;
import com.yanny.ali.plugin.glm.IGlobalLootModifierPlugin;
import com.yanny.ali.plugin.glm.ILootTableIdConditionPredicate;
import com.yanny.alicompat.platform.Services;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.function.Consumer;

public final class ModCompatManager {
    private static final Logger LOGGER = CommonLogUtils.getLogger(Utils.MOD_ID);

    private static volatile List<IModCompat> compats;

    public static void registerCommon(ICommonRegistry registry) {
        collectTranslations(true).keySet().forEach(registry::registerTranslationKey);
        forEach("common", (compat) -> compat.registerCommon(registry));
    }

    @NotNull
    public static Map<String, String> collectTranslations(boolean loadedOnly) {
        Map<String, String> translations = new HashMap<>();

        try {
            ServiceLoader.load(ICompatTranslations.class, ICompatTranslations.class.getClassLoader()).stream().forEach((provider) -> {
                try {
                    ICompatTranslations compatTranslations = provider.get();

                    if (!loadedOnly || Services.getPlatform().isModLoaded(compatTranslations.targetModId())) {
                        translations.putAll(compatTranslations.getTranslations());
                    }
                } catch (Throwable e) {
                    LOGGER.warn("Failed to collect translations with error: {}", e.getMessage(), e);
                }
            });
        } catch (Throwable e) {
            LOGGER.warn("Failed to enumerate translations with error: {}", e.getMessage(), e);
        }

        return translations;
    }

    public static void registerClient(IClientRegistry registry) {
        forEach("client", (compat) -> compat.registerClient(registry));
    }

    public static void registerServer(IServerRegistry registry) {
        forEach("server", (compat) -> compat.registerServer(registry));
    }

    public static void registerGlobalLootModifier(IGlobalLootModifierPlugin.IRegistry registry, ILootTableIdConditionPredicate predicate) {
        forEach("global loot modifier", (compat) -> {
            if (compat instanceof IGlmModCompat glmCompat) {
                glmCompat.registerGlobalLootModifier(registry, predicate);
            }
        });
    }

    private static void forEach(String part, Consumer<IModCompat> action) {
        for (IModCompat compat : getCompats()) {
            try {
                action.accept(compat);
            } catch (Throwable e) {
                LOGGER.error("Failed to register {} part of mod compat for [{}] with error: {}", part, compat.targetModId(), e.getMessage(), e);
            }
        }
    }

    @NotNull
    private static List<IModCompat> getCompats() {
        if (compats == null) {
            synchronized (ModCompatManager.class) {
                if (compats == null) {
                    compats = load();
                }
            }
        }
        return compats;
    }

    @NotNull
    private static List<IModCompat> load() {
        List<IModCompat> enabled = new ArrayList<>();
        Iterator<IModCompat> iterator = ServiceLoader.load(IModCompat.class, IModCompat.class.getClassLoader()).iterator();

        while (true) {
            IModCompat compat;

            try {
                if (!iterator.hasNext()) {
                    break;
                }

                compat = iterator.next();
            } catch (Throwable e) {
                // resolving a compat whose target mod is absent throws before targetModId() can be asked; the iterator has moved past it
                LOGGER.debug("Skipped mod compat, target mod is not loaded: {}", e.getMessage());
                continue;
            }

            try {
                if (Services.getPlatform().isModLoaded(compat.targetModId())) {
                    enabled.add(compat);
                    LOGGER.info("Enabled mod compat for [{}] {}", compat.targetModId(), compat.getClass().getCanonicalName());
                } else {
                    LOGGER.debug("Skipped mod compat {}, mod is not loaded", compat.getClass().getCanonicalName());
                }
            } catch (Throwable e) {
                LOGGER.warn("Failed to load mod compat with error: {}", e.getMessage(), e);
            }
        }

        LOGGER.info("Found {} mod compat(s)", enabled.size());
        return Collections.unmodifiableList(enabled);
    }

    private ModCompatManager() {}
}

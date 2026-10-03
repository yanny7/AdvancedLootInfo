package com.yanny.aci.manager;

import com.yanny.aci.CommonLogUtils;
import com.yanny.aci.api.ICoreCommonUtils;
import com.yanny.aci.api.ICoreServerUtils;
import com.yanny.aci.api.NumberConverter;
import com.yanny.aci.api.NumberExpr;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.aci.tooltip.TooltipNodePalette;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.util.valueproviders.IntProvider;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class CoreServerRegistry<
        TConfig,
        TCommonUtils extends CoreCommonRegistry<TConfig>,
        TServerUtils extends ICoreServerUtils<?>
        >
        extends
        BaseRegistry
        implements
        ICoreServerUtils<TServerUtils>,
        ICoreCommonUtils<TConfig> {
    private final Logger logger;
    private final ServerLevel serverLevel;
    private final TooltipNodePalette tooltipNodeCache;
    private final List<Runnable> cacheCleaners = new ArrayList<>();
    private final ManagedRegistry<Class<?>, NumberConverter<TServerUtils, IntProvider>> intProviders = registerClassKeyed("int providers", true, HashMap::new, BuiltInRegistries.INT_PROVIDER_TYPE);
    private final ManagedRegistry<Class<?>, NumberConverter<TServerUtils, FloatProvider>> floatProviders = registerClassKeyed("float providers", true, HashMap::new, BuiltInRegistries.FLOAT_PROVIDER_TYPE);
    protected final TCommonUtils commonUtils;

    public CoreServerRegistry(TCommonUtils registry, ServerLevel level) {
        super(registry.getModId());
        logger = CommonLogUtils.getLogger(registry.getModId());
        tooltipNodeCache = new TooltipNodePalette(registry.getModId());
        commonUtils = registry;
        serverLevel = level;
    }

    @NotNull
    @Override
    public ServerLevel getServerLevel() {
        return serverLevel;
    }

    @NotNull
    @Override
    public HolderLookup.Provider lookupProvider() {
        return serverLevel.registryAccess();
    }

    @NotNull
    @Override
    public TConfig getConfiguration() {
        return commonUtils.getConfiguration();
    }

    @NotNull
    @Override
    public TooltipNodePalette getTooltipCache() {
        return tooltipNodeCache;
    }

    public void registerCacheCleaner(Runnable cleaner) {
        cacheCleaners.add(cleaner);
    }

    public <T extends IntProvider> void registerIntProvider(Class<T> type, NumberConverter<TServerUtils, T> converter) {
        //noinspection unchecked
        intProviders.put(type, (u, t, c) -> converter.convert(u, (T) t, c));
    }

    public <T extends FloatProvider> void registerFloatProvider(Class<T> type, NumberConverter<TServerUtils, T> converter) {
        //noinspection unchecked
        floatProviders.put(type, (u, t, c) -> converter.convert(u, (T) t, c));
    }

    @NotNull
    @Override
    public NumberExpr convertIntProvider(TServerUtils utils, IntProvider provider, List<TooltipNode> conditions) {
        return NumberConverters.convert(getModId(), intProviders, utils, provider, conditions, (p) -> String.valueOf(BuiltInRegistries.INT_PROVIDER_TYPE.getKey(p.codec())));
    }

    @NotNull
    @Override
    public NumberExpr convertFloatProvider(TServerUtils utils, FloatProvider provider, List<TooltipNode> conditions) {
        return NumberConverters.convert(getModId(), floatProviders, utils, provider, conditions, (p) -> String.valueOf(BuiltInRegistries.FLOAT_PROVIDER_TYPE.getKey(p.codec())));
    }

    public void clearCaches() {
        for (Runnable cleaner : cacheCleaners) {
            try {
                cleaner.run();
            } catch (Throwable e) {
                logger.warn("Failed to clear cache: {}", e.getMessage(), e);
            }
        }

        tooltipNodeCache.clear();
    }

    @Override
    public void clearData() {
        super.clearData();
        cacheCleaners.clear();
    }

    @Override
    public int getTranslationKeyIndex(String key) {
        Integer value = commonUtils.getDictionary().getOrDefault(key, -1);
        return value == null ? -1 : value;
    }
}

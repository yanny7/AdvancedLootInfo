package com.yanny.aci.manager;

import com.yanny.aci.api.ICoreCommonUtils;
import com.yanny.aci.api.ICoreServerUtils;
import com.yanny.aci.tooltip.TooltipNodePalette;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.NotNull;

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
    private final ServerLevel serverLevel;
    private final TooltipNodePalette tooltipNodeCache = new TooltipNodePalette();
    protected final TCommonUtils commonUtils;

    public CoreServerRegistry(TCommonUtils registry, ServerLevel level) {
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
    public TConfig getConfiguration() {
        return commonUtils.getConfiguration();
    }

    @NotNull
    @Override
    public TooltipNodePalette getTooltipCache() {
        return tooltipNodeCache;
    }

    @Override
    public int getTranslationKeyIndex(String key) {
        Integer value = commonUtils.getDictionary().getOrDefault(key, -1);
        return value == null ? -1 : value;
    }
}

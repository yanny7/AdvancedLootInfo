package com.yanny.alicompat.compat.kaleidoscopecookery;

import com.github.ysbbbbbb.kaleidoscopecookery.loot.AdvanceBlockMatchTool;
import com.github.ysbbbbbb.kaleidoscopecookery.loot.AdvanceEntityMatchTool;
import com.yanny.ali.api.IServerRegistry;
import com.yanny.alicompat.IModCompat;
import com.yanny.alicompat.accessor.PluginUtils;
import org.jetbrains.annotations.NotNull;

public class KaleidoscopeCookeryCompat implements IModCompat {
    @NotNull
    @Override
    public String targetModId() {
        return KaleidoscopeCookeryLang.MOD_ID;
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        PluginUtils.registerConditionTooltip(registry, AdvanceBlockMatchTool.class, AdvanceBlockMatchToolAccessor.class);
        PluginUtils.registerConditionTooltip(registry, AdvanceEntityMatchTool.class, AdvanceEntityMatchToolAccessor.class);
    }
}

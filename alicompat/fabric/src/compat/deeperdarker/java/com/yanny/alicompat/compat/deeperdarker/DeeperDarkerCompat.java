package com.yanny.alicompat.compat.deeperdarker;

import com.kyanite.deeperdarker.content.DDDataComponents;
import com.kyanite.deeperdarker.util.SetPaintingVariantFunction;
import com.yanny.ali.api.IServerRegistry;
import com.yanny.ali.plugin.server.DataComponentTooltipUtils;
import com.yanny.alicompat.IModCompat;
import com.yanny.alicompat.accessor.PluginUtils;
import org.jetbrains.annotations.NotNull;

public class DeeperDarkerCompat implements IModCompat {
    @NotNull
    @Override
    public String targetModId() {
        return DeeperDarkerLang.MOD_ID;
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        PluginUtils.registerFunctionTooltip(registry, SetPaintingVariantFunction.class, SetPaintingVariantFunctionAccessor.class);

        registry.registerDataComponentTypeTooltip(DDDataComponents.HAS_FLOWER, DataComponentTooltipUtils::getBoolTooltip);
    }
}

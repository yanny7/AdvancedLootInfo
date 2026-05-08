package com.yanny.awi.plugin.server;

import com.mojang.logging.LogUtils;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.awi.api.IServerUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import static com.yanny.aci.tooltip.CoreTooltipUtils.getBuiltInRegistryTooltip;

public class RegistriesTooltipUtils {
    private static final Logger LOGGER = LogUtils.getLogger();

    @NotNull
    public static TooltipBuilder getBlockTooltip(IServerUtils utils, Block block) {
//        if (utils.getConfiguration().showInGameNames) { FIXME
            try {
                return TooltipBuilder.value(block.getName());
            } catch (Throwable e) {
                LOGGER.warn("Failed to get localized Block name: {}", BuiltInRegistries.BLOCK.getKey(block), e);
            }
//        }

        return getBuiltInRegistryTooltip(utils, BuiltInRegistries.BLOCK, block);
    }
}

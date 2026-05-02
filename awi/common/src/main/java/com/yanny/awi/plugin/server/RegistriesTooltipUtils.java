package com.yanny.awi.plugin.server;

import com.mojang.logging.LogUtils;
import com.yanny.awi.api.IKeyTooltipNode;
import com.yanny.awi.api.IServerUtils;
import com.yanny.awi.plugin.common.tooltip.ComponentTooltipNode;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import static com.yanny.aci.tooltip.CoreTooltipUtils.getBuiltInRegistryTooltip;

public class RegistriesTooltipUtils {
    private static final Logger LOGGER = LogUtils.getLogger();

    @NotNull
    public static IKeyTooltipNode getBlockTooltip(IServerUtils utils, Block block) {
//        if (utils.getConfiguration().showInGameNames) { FIXME
            try {
                return ComponentTooltipNode.values(block.getName());
            } catch (Throwable e) {
                LOGGER.warn("Failed to get localized Block name: {}", BuiltInRegistries.BLOCK.getKey(block), e);
            }
//        }

        return getBuiltInRegistryTooltip(utils, BuiltInRegistries.BLOCK, block);
    }
}

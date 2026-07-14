package com.yanny.awi.plugin.server;

import com.yanny.awi.api.IServerUtils;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.stateproviders.RuleBasedBlockStateProvider;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class CollectorUtils {
    @NotNull
    public static List<Block> collectRule(IServerUtils utils, RuleBasedBlockStateProvider provider) {
        List<Block> blocks = new ArrayList<>(utils.collectBlocks(utils, provider.fallback()));

        for (RuleBasedBlockStateProvider.Rule rule : provider.rules()) {
            blocks.addAll(utils.collectBlocks(utils, rule.then()));
        }

        return blocks;
    }
}

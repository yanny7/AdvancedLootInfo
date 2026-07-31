package com.yanny.awi.plugin.server;

import com.yanny.awi.api.IServerUtils;
import net.minecraft.core.Holder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.rootplacers.MangroveRootPlacer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class RootPlacerCollectorUtils {
    @NotNull
    public static List<Block> collectMangrove(IServerUtils utils, MangroveRootPlacer placer) {
        List<Block> blocks = new ArrayList<>();

        blocks.addAll(placer.mangroveRootPlacement.muddyRootsIn().stream().map(Holder::value).toList());
        blocks.addAll(utils.collectBlocks(utils, placer.mangroveRootPlacement.muddyRootsProvider()));
        return blocks;
    }
}

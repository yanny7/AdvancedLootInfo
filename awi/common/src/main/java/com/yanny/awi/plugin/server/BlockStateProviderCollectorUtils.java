package com.yanny.awi.plugin.server;

import com.yanny.awi.api.IServerUtils;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.levelgen.feature.stateproviders.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BlockStateProviderCollectorUtils {
    @Unmodifiable
    @NotNull
    public static List<Block> collectSimple(IServerUtils ignoredUtils, SimpleStateProvider provider) {
        return Collections.singletonList(provider.state.getBlock());
    }

    @Unmodifiable
    @NotNull
    public static List<Block> collectNoise(IServerUtils ignoredUtils, NoiseProvider provider) {
        return provider.states.stream().map(BlockBehaviour.BlockStateBase::getBlock).toList();
    }

    @NotNull
    public static List<Block> collectNoiseThreshold(IServerUtils ignoredUtils, NoiseThresholdProvider provider) {
        List<Block> blocks = new ArrayList<>();

        blocks.add(provider.defaultState.getBlock());
        blocks.addAll(provider.lowStates.stream().map(BlockBehaviour.BlockStateBase::getBlock).toList());
        blocks.addAll(provider.highStates.stream().map(BlockBehaviour.BlockStateBase::getBlock).toList());
        return blocks;
    }

    @NotNull
    public static List<Block> collectRandomized(IServerUtils utils, RandomizedIntStateProvider provider) {
        return utils.collectBlocks(utils, provider.source);
    }

    @Unmodifiable
    @NotNull
    public static List<Block> collectRotated(IServerUtils ignoredUtils, RotatedBlockProvider provider) {
        return Collections.singletonList(provider.block);
    }

    @Unmodifiable
    @NotNull
    public static List<Block> collectWeighted(IServerUtils ignoredUtils, WeightedStateProvider provider) {
        return provider.weightedList.unwrap().stream().map((entry) -> entry.value().getBlock()).toList();
    }
}

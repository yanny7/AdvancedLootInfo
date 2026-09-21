package com.yanny.awi.plugin.server;

import com.yanny.awi.api.IServerUtils;
import net.minecraft.core.Holder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.levelgen.feature.stateproviders.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BlockStateProviderCollectorUtils {
    @NotNull
    public static List<Block> collectHolder(IServerUtils utils, @Nullable Holder<BlockStateProvider> holder) {
        if (holder == null || !holder.isBound()) {
            return List.of();
        }

        return utils.collectBlocks(utils, holder.value());
    }

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

    @Unmodifiable
    @NotNull
    public static List<Block> collectDualNoise(IServerUtils utils, DualNoiseProvider provider) {
        return collectNoise(utils, provider);
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
        return collectHolder(utils, provider.source);
    }

    @NotNull
    public static List<Block> collectRotated(IServerUtils utils, RotatedBlockProvider provider) {
        return collectHolder(utils, provider.state());
    }

    @Unmodifiable
    @NotNull
    public static List<Block> collectWeighted(IServerUtils ignoredUtils, WeightedStateProvider provider) {
        return provider.weightedList.unwrap().stream().map((entry) -> entry.value().getBlock()).toList();
    }

    @NotNull
    public static List<Block> collectRuleBased(IServerUtils utils, RuleBasedStateProvider provider) {
        return collectHolder(utils, provider.fallback());
    }
}

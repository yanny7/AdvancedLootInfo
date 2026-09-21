package com.yanny.awi.plugin.server;

import com.yanny.awi.api.IServerUtils;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.treedecorators.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collections;
import java.util.List;

public class TreeDecoratorCollectorUtils {
    @Unmodifiable
    @NotNull
    public static List<Block> collectTrunkVine(IServerUtils ignoredUtils, TrunkVineDecorator ignoredDecorator) {
        return Collections.emptyList();
    }

    @Unmodifiable
    @NotNull
    public static List<Block> collectLeaveVine(IServerUtils ignoredUtils, LeaveVineDecorator ignoredDecorator) {
        return Collections.emptyList();
    }

    @Unmodifiable
    @NotNull
    public static List<Block> collectLPaleMoss(IServerUtils ignoredUtils, PaleMossDecorator ignoredDecorator) {
        return Collections.emptyList();
    }

    @Unmodifiable
    @NotNull
    public static List<Block> collectCreakingHeart(IServerUtils ignoredUtils, CreakingHeartDecorator ignoredDecorator) {
        return Collections.emptyList();
    }

    @Unmodifiable
    @NotNull
    public static List<Block> collectShelfMushroom(IServerUtils ignoredUtils, ShelfMushroomDecorator ignoredDecorator) {
        return Collections.emptyList();
    }

    @Unmodifiable
    @NotNull
    public static List<Block> collectCocoa(IServerUtils ignoredUtils, CocoaDecorator ignoredDecorator) {
        return Collections.emptyList();
    }

    @Unmodifiable
    @NotNull
    public static List<Block> collectBeehive(IServerUtils ignoredUtils, BeehiveDecorator ignoredDecorator) {
        return Collections.emptyList();
    }

    @NotNull
    public static List<Block> collectAlterGround(IServerUtils utils, AlterGroundDecorator decorator) {
        return BlockStateProviderCollectorUtils.collectHolder(utils, decorator.provider);
    }

    @NotNull
    public static List<Block> collectAttachedToLeaves(IServerUtils utils, AttachedToLeavesDecorator decorator) {
        return BlockStateProviderCollectorUtils.collectHolder(utils, decorator.blockProvider);
    }

    @NotNull
    public static List<Block> collectPlaceOnGround(IServerUtils utils, PlaceOnGroundDecorator decorator) {
        return BlockStateProviderCollectorUtils.collectHolder(utils, decorator.blockStateProvider);
    }

    @NotNull
    public static List<Block> collectAttachedToLogs(IServerUtils utils, AttachedToLogsDecorator decorator) {
        return BlockStateProviderCollectorUtils.collectHolder(utils, decorator.blockProvider);
    }
}

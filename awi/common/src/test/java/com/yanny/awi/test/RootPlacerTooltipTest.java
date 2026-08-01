package com.yanny.awi.test;

import com.yanny.awi.plugin.server.RootPlacerTooltipUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.rootplacers.AboveRootPlacement;
import net.minecraft.world.level.levelgen.feature.rootplacers.MangroveRootPlacement;
import net.minecraft.world.level.levelgen.feature.rootplacers.MangroveRootPlacer;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static com.yanny.awi.test.TooltipTestSuite.UTILS;
import static com.yanny.awi.test.utils.TestUtils.assertTooltip;

public class RootPlacerTooltipTest {
    @Test
    public void testMangroveRootPlacerTooltip() {
        assertTooltip(RootPlacerTooltipUtils.getMangroveRootPlacerTooltip(UTILS, new MangroveRootPlacer(
                ConstantInt.of(2),
                BlockStateProvider.simple(Blocks.MANGROVE_ROOTS),
                Optional.of(
                        new AboveRootPlacement(
                                BlockStateProvider.simple(Blocks.AMETHYST_BLOCK),
                                0.5f
                        )
                ),
                new MangroveRootPlacement(
                        HolderSet.direct(Holder.direct(Blocks.MUD), Holder.direct(Blocks.MUDDY_MANGROVE_ROOTS)),
                        HolderSet.direct(Holder.direct(Blocks.MUD), Holder.direct(Blocks.MANGROVE_ROOTS)),
                        BlockStateProvider.simple(Blocks.MUDDY_MANGROVE_ROOTS),
                        8,
                        15,
                        0.5f
                )
        )).build(), List.of(
                "Mangrove Root:",
                "  -> Trunk Offset Y:",
                "    -> Constant:",
                "      -> Value: 2",
                "  -> Root Provider:",
                "    -> Simple:",
                "      -> State:",
                "        -> Block: Mangrove Roots",
                "        -> Properties:",
                "          -> waterlogged: false",
                "  -> Above Root Placement:",
                "    -> Above Root Provider:",
                "      -> Simple:",
                "        -> State:",
                "          -> Block: Block of Amethyst",
                "    -> Placement Chance: 0.5",
                "  -> Mangrove Root Placement:",
                "    -> Can Grow Through:",
                "      -> Mud",
                "      -> Muddy Mangrove Roots",
                "    -> Muddy Root In:",
                "      -> Mud",
                "      -> Mangrove Roots",
                "    -> Muddy Root Provider:",
                "      -> Simple:",
                "        -> State:",
                "          -> Block: Muddy Mangrove Roots",
                "          -> Properties:",
                "            -> axis: y",
                "    -> Max Root Width: 8",
                "    -> Max Root Length: 15",
                "    -> Random Skew Chance: 0.5"
        ));
    }
}

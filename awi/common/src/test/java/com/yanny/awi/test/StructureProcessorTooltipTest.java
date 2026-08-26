package com.yanny.awi.test;

import com.yanny.awi.plugin.server.StructureProcessorTooltipUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.templatesystem.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.yanny.awi.test.TooltipTestSuite.LOOKUP;
import static com.yanny.awi.test.TooltipTestSuite.UTILS;
import static com.yanny.aci.test.utils.TestUtils.assertTooltip;

public class StructureProcessorTooltipTest {
    @Test
    public void testBlockIgnoreProcessorTooltip() {
        assertTooltip(StructureProcessorTooltipUtils.getBlockIgnoreProcessorTooltip(UTILS, BlockIgnoreProcessor.AIR).build(), List.of(
                "Block Ignore:",
                "  -> To Ignore: Air"
        ));
        assertTooltip(StructureProcessorTooltipUtils.getBlockIgnoreProcessorTooltip(UTILS, new BlockIgnoreProcessor(
                List.of(Blocks.STONE, Blocks.ANDESITE)
        )).build(), List.of(
                "Block Ignore:",
                "  -> To Ignore:",
                "    -> Stone",
                "    -> Andesite"
        ));
    }

    @Test
    public void testBlockRotProcessorTooltip() {
        assertTooltip(StructureProcessorTooltipUtils.getBlockRotProcessorTooltip(UTILS, new BlockRotProcessor(
                HolderSet.direct(Holder.direct(Blocks.STONE)),
                0.5f
        )).build(), List.of(
                "Block Rot:",
                "  -> Rottable Block: Stone",
                "  -> Integrity: 0.5"
        ));
        assertTooltip(StructureProcessorTooltipUtils.getBlockRotProcessorTooltip(UTILS, new BlockRotProcessor(
                LOOKUP.lookupOrThrow(Registries.BLOCK).getOrThrow(BlockTags.LOGS),
                0.5f
        )).build(), List.of(
                "Block Rot:",
                "  -> Rottable Blocks:",
                "    -> Tag: minecraft:logs",
                "  -> Integrity: 0.5"
        ));
    }

    @Test
    public void testGravityProcessorTooltip() {
        assertTooltip(StructureProcessorTooltipUtils.getGravityProcessorTooltip(UTILS, new GravityProcessor(Heightmap.Types.WORLD_SURFACE_WG, 1)).build(), List.of(
                "Gravity:",
                "  -> Heightmap: Highest Block, Plants Included",
                "  -> Offset: 1"
        ));
    }

    @Test
    public void testJigsawReplacementProcessorTooltip() {
        assertTooltip(StructureProcessorTooltipUtils.getJigsawReplacementProcessorTooltip(UTILS, JigsawReplacementProcessor.INSTANCE).build(), List.of(
                "Jigsaw Replacement"
        ));
    }

    @Test
    public void testRuleProcessorTooltip() {
        assertTooltip(StructureProcessorTooltipUtils.getRuleProcessorTooltip(UTILS, new RuleProcessor(List.of(
                new ProcessorRule(AlwaysTrueTest.INSTANCE, AlwaysTrueTest.INSTANCE, Blocks.STONE.defaultBlockState())
        ))).build(), List.of(
                "Rule:",
                "  -> Rules:",
                "    -> Input Predicate:",
                "      -> Always True",
                "    -> Location Predicate:",
                "      -> Always True",
                "    -> Position Predicate: minecraft:always_true",
                "    -> Output State:",
                "      -> Block: Stone",
                "    -> Block Entity Modifier: minecraft:passthrough"
        ));
    }

    @Test
    public void testNopProcessorTooltip() {
        assertTooltip(StructureProcessorTooltipUtils.getNopProcessorTooltip(UTILS, NopProcessor.INSTANCE).build(), List.of(
                "Nop"
        ));
    }

    @Test
    public void testBlockAgeProcessorTooltip() {
        assertTooltip(StructureProcessorTooltipUtils.getBlockAgeProcessorTooltip(UTILS, new BlockAgeProcessor(0.5f)).build(), List.of(
                "Block Age:",
                "  -> Mossiness: 0.5"
        ));
    }

    @Test
    public void testBlackstoneReplaceProcessorTooltip() {
        assertTooltip(StructureProcessorTooltipUtils.getBlackstoneReplaceProcessorTooltip(UTILS, BlackstoneReplaceProcessor.INSTANCE).build(), List.of(
                "Blackstone Replace"
        ));
    }

    @Test
    public void testLavaSubmergedBlockProcessorTooltip() {
        assertTooltip(StructureProcessorTooltipUtils.getLavaSubmergedBlockProcessorTooltip(UTILS, new LavaSubmergedBlockProcessor()).build(), List.of(
                "Lava Submerged Block"
        ));
    }

    @Test
    public void testProtectedBlockProcessorTooltip() {
        assertTooltip(StructureProcessorTooltipUtils.getProtectedBlockProcessorTooltip(UTILS, new ProtectedBlockProcessor(
                LOOKUP.lookupOrThrow(Registries.BLOCK).getOrThrow(BlockTags.WOOL)
        )).build(), List.of(
                "Protected Blocks:",
                "  -> Cannot Replace:",
                "    -> Tag: minecraft:wool"
        ));
    }

    @Test
    public void testCappedProcessorTooltip() {
        assertTooltip(StructureProcessorTooltipUtils.getCappedProcessorTooltip(UTILS, new CappedProcessor(NopProcessor.INSTANCE, ConstantInt.of(5))).build(), List.of(
                "Capped:",
                "  -> Delegate:",
                "    -> Nop",
                "  -> Limit:",
                "    -> Constant:",
                "      -> Value: 5"
        ));
    }
}

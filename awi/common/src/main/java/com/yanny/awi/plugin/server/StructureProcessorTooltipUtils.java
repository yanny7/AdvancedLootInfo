package com.yanny.awi.plugin.server;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.awi.api.IServerUtils;
import com.yanny.awi.language.Lang;
import net.minecraft.world.level.levelgen.structure.templatesystem.*;
import org.jetbrains.annotations.NotNull;

import static com.yanny.aci.tooltip.TooltipBuilder.array;

public class StructureProcessorTooltipUtils {
    @NotNull
    public static TooltipBuilder getBlockIgnoreProcessorTooltip(IServerUtils utils, BlockIgnoreProcessor processor) {
        return array((b) -> b.add(utils.getValueTooltip(utils, processor.toIgnore).build(Lang.Branch.TO_IGNORE)), Lang.StructureProcessor.BLOCK_IGNORE);
    }

    @NotNull
    public static TooltipBuilder getBlockRotProcessorTooltip(IServerUtils utils, BlockRotProcessor processor) {
        return array((b) -> {
            b.add(utils.getValueTooltip(utils, processor.rottableBlocks).build(Lang.Branch.ROTTABLE_BLOCKS));
            b.add(utils.getValueTooltip(utils, processor.integrity).build(Lang.Value.INTEGRITY));
        }, Lang.StructureProcessor.BLOCK_ROT);
    }

    @NotNull
    public static TooltipBuilder getGravityProcessorTooltip(IServerUtils utils, GravityProcessor processor) {
        return array((b) -> {
            b.add(utils.getValueTooltip(utils, processor.heightmap).build(Lang.Value.HEIGHTMAP));
            b.add(utils.getValueTooltip(utils, processor.offset).build(Lang.Value.OFFSET));
        }, Lang.StructureProcessor.GRAVITY);
    }

    @NotNull
    public static TooltipBuilder getJigsawReplacementProcessorTooltip(IServerUtils ignoredUtils, JigsawReplacementProcessor ignoredProcessor) {
        return TooltipBuilder.array(TooltipBuilder::showEmpty, Lang.StructureProcessor.JIGSAW_REPLACEMENT);
    }

    @NotNull
    public static TooltipBuilder getRuleProcessorTooltip(IServerUtils utils, RuleProcessor processor) {
        return array((b) -> b.add(utils.getValueTooltip(utils, processor.rules).build(Lang.Branch.RULES)), Lang.StructureProcessor.RULE);
    }

    @NotNull
    public static TooltipBuilder getNopProcessorTooltip(IServerUtils ignoredUtils, NopProcessor ignoredProcessor) {
        return TooltipBuilder.array(TooltipBuilder::showEmpty, Lang.StructureProcessor.NOP);
    }

    @NotNull
    public static TooltipBuilder getBlockAgeProcessorTooltip(IServerUtils utils, BlockAgeProcessor processor) {
        return array((b) -> b.add(utils.getValueTooltip(utils, processor.mossiness).build(Lang.Value.MOSSINESS)), Lang.StructureProcessor.BLOCK_AGE);
    }

    @NotNull
    public static TooltipBuilder getBlackstoneReplaceProcessorTooltip(IServerUtils ignoredUtils, BlackstoneReplaceProcessor ignoredProcessor) {
        return TooltipBuilder.array(TooltipBuilder::showEmpty, Lang.StructureProcessor.BLACKSTONE_REPLACE);
    }

    @NotNull
    public static TooltipBuilder getLavaSubmergedBlockProcessorTooltip(IServerUtils ignoredUtils, LavaSubmergedBlockProcessor ignoredProcessor) {
        return TooltipBuilder.array(TooltipBuilder::showEmpty, Lang.StructureProcessor.LAVA_SUBMERGED_BLOCK);
    }

    @NotNull
    public static TooltipBuilder getProtectedBlockProcessorTooltip(IServerUtils utils, ProtectedBlockProcessor processor) {
        return array((b) -> b.add(utils.getValueTooltip(utils, processor.cannotReplace()).build(Lang.Branch.CANNOT_REPLACE)), Lang.StructureProcessor.PROTECTED_BLOCKS);
    }

    @NotNull
    public static TooltipBuilder getCappedProcessorTooltip(IServerUtils utils, CappedProcessor processor) {
        return array((b) -> {
            b.add(utils.getValueTooltip(utils, processor.delegate).build(Lang.Branch.DELEGATE));
            b.add(utils.getValueTooltip(utils, processor.limit).build(Lang.Branch.LIMIT));
        }, Lang.StructureProcessor.CAPPED);
    }
}

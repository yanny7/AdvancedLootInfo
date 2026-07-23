package com.yanny.awi.plugin.server;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.awi.api.IServerUtils;
import com.yanny.awi.language.Lang;
import net.minecraft.core.Vec3i;
import net.minecraft.util.random.Weight;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.GeodeBlockSettings;
import net.minecraft.world.level.levelgen.GeodeCrackSettings;
import net.minecraft.world.level.levelgen.GeodeLayerSettings;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.SpikeFeature;
import net.minecraft.world.level.levelgen.feature.WeightedPlacedFeature;
import net.minecraft.world.level.levelgen.feature.configurations.BlockColumnConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.feature.featuresize.FeatureSize;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.rootplacers.RootPlacer;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import org.jetbrains.annotations.NotNull;

public class ValueTooltipUtils {
    @NotNull
    public static TooltipBuilder getIntProviderTooltip(IServerUtils utils, IntProvider value) {
        return utils.getIntProviderTooltip(utils, value);
    }

    @NotNull
    public static TooltipBuilder getRuleTestTooltip(IServerUtils utils, RuleTest value) {
        return utils.getRuleTestTooltip(utils, value);
    }

    @NotNull
    public static TooltipBuilder getHeightProviderTooltip(IServerUtils utils, HeightProvider value) {
        return utils.getHeightProviderTooltip(utils, value);
    }

    @NotNull
    public static TooltipBuilder getBlockPredicateTooltip(IServerUtils utils, BlockPredicate value) {
        return utils.getBlockPredicateTooltip(utils, value);
    }

    @NotNull
    public static TooltipBuilder getBlockStateProviderTooltip(IServerUtils utils, BlockStateProvider value) {
        return utils.getBlockStateProviderTooltip(utils, value);
    }

    @NotNull
    public static TooltipBuilder getTreeDecoratorTooltip(IServerUtils utils, TreeDecorator value) {
        return utils.getTreeDecoratorTooltip(utils, value);
    }

    @NotNull
    public static TooltipBuilder getFeatureSizeTooltip(IServerUtils utils, FeatureSize value) {
        return utils.getFeatureSizeTooltip(utils, value);
    }

    @NotNull
    public static TooltipBuilder getRootPlacerTooltip(IServerUtils utils, RootPlacer value) {
        return utils.getRootPlacerTooltip(utils, value);
    }

    @NotNull
    public static TooltipBuilder getFoliagePlacerTooltip(IServerUtils utils, FoliagePlacer value) {
        return utils.getFoliagePlacerTooltip(utils, value);
    }

    @NotNull
    public static TooltipBuilder getTrunkPlacerTooltip(IServerUtils utils, TrunkPlacer value) {
        return utils.getTrunkPlacerTooltip(utils, value);
    }

    @NotNull
    public static TooltipBuilder getFloatProviderTooltip(IServerUtils utils, FloatProvider value) {
        return utils.getFloatProviderTooltip(utils, value);
    }

    @NotNull
    public static TooltipBuilder getTargetBlockStateTooltip(IServerUtils utils, OreConfiguration.TargetBlockState value) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, value.state).build(Lang.Branch.STATE));
            b.add(utils.getValueTooltip(utils, value.target).build(Lang.Branch.TARGET));
        });
    }

    @NotNull
    public static TooltipBuilder getBlockStateTooltip(IServerUtils utils, BlockState value) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, value.getBlock()).build(Lang.Value.BLOCK));

            TooltipBuilder array = TooltipBuilder.array((c) -> {
                value.getValues().forEach((p, v) -> c.add(TooltipBuilder.keyValue(p.getName(), v.toString())));
            });

            b.add(array.build(Lang.Branch.PROPERTIES));
        });
    }

    @NotNull
    public static TooltipBuilder getVec3iTooltip(IServerUtils utils, Vec3i value) {
        return utils.getValueTooltip(utils, "[" + value.getX() + "," + value.getY() + "," + value.getZ() + "]");
    }

    @NotNull
    public static TooltipBuilder getWeightedEntryWrapperTooltip(IServerUtils utils, WeightedEntry.Wrapper<?> value) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, value.getWeight()).build(Lang.Value.WEIGHT));
            b.add(utils.getValueTooltip(utils, value.getData()).build(Lang.Branch.DATA));
        }).key(Lang.Branch.ENTRY);
    }

    @NotNull
    public static TooltipBuilder getWeightTooltip(IServerUtils utils, Weight value) {
        return utils.getValueTooltip(utils, value.asInt());
    }

    @NotNull
    public static TooltipBuilder getBlockColumnConfigurationLayerTooltip(IServerUtils utils, BlockColumnConfiguration.Layer value) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, value.height()).build(Lang.Value.HEIGHT));
            b.add(utils.getValueTooltip(utils, value.state()).build(Lang.Branch.STATE));
        }).key(Lang.Branch.ENTRY);
    }

    @NotNull
    public static TooltipBuilder getGeodeBlockSettingsTooltip(IServerUtils utils, GeodeBlockSettings value) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, value.fillingProvider).build(Lang.Branch.FILLING_PROVIDER));
            b.add(utils.getValueTooltip(utils, value.innerLayerProvider).build(Lang.Branch.INNER_LAYER_PROVIDER));
            b.add(utils.getValueTooltip(utils, value.alternateInnerLayerProvider).build(Lang.Branch.ALTERNATE_INNER_LAYER_PROVIDER));
            b.add(utils.getValueTooltip(utils, value.middleLayerProvider).build(Lang.Branch.MIDDLE_LAYER_PROVIDER));
            b.add(utils.getValueTooltip(utils, value.outerLayerProvider).build(Lang.Branch.OUTER_LAYER_PROVIDER));
            b.add(utils.getValueTooltip(utils, value.innerPlacements).build(Lang.Branch.INNER_PLACEMENTS));
            b.add(utils.getValueTooltip(utils, value.cannotReplace).build(Lang.Branch.CANNOT_REPLACE));
            b.add(utils.getValueTooltip(utils, value.invalidBlocks).build(Lang.Branch.INVALID_BLOCKS));
        });
    }

    @NotNull
    public static TooltipBuilder getGeodeLayerSettingsTooltip(IServerUtils utils, GeodeLayerSettings value) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, value.filling).build(Lang.Value.FILLING));
            b.add(utils.getValueTooltip(utils, value.innerLayer).build(Lang.Value.INNER_LAYER));
            b.add(utils.getValueTooltip(utils, value.middleLayer).build(Lang.Value.MIDDLE_LAYER));
            b.add(utils.getValueTooltip(utils, value.outerLayer).build(Lang.Value.OUTER_LAYER));
        });
    }

    @NotNull
    public static TooltipBuilder getGeodeCrackSettingsTooltip(IServerUtils utils, GeodeCrackSettings value) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, value.generateCrackChance).build(Lang.Value.GENERATE_CRACK_CHANCE));
            b.add(utils.getValueTooltip(utils, value.baseCrackSize).build(Lang.Value.BASE_CRACK_SIZE));
            b.add(utils.getValueTooltip(utils, value.crackPointOffset).build(Lang.Value.CRACK_POINT_OFFSET));
        });
    }

    @NotNull
    public static TooltipBuilder getWeightedPlacedFeatureTooltip(IServerUtils utils, WeightedPlacedFeature value) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, value.feature).build(Lang.Branch.FEATURE));
            b.add(utils.getValueTooltip(utils, value.chance).build(Lang.Value.CHANCE));
        });
    }

    @NotNull
    public static TooltipBuilder getEndSpikeTooltip(IServerUtils utils, SpikeFeature.EndSpike value) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, value.getCenterX()).build(Lang.Value.CENTER_X));
            b.add(utils.getValueTooltip(utils, value.getCenterZ()).build(Lang.Value.CENTER_Z));
            b.add(utils.getValueTooltip(utils, value.getRadius()).build(Lang.Value.RADIUS));
            b.add(utils.getValueTooltip(utils, value.getHeight()).build(Lang.Value.HEIGHT));
            b.add(utils.getValueTooltip(utils, value.isGuarded()).build(Lang.Value.IS_GUARDED));
        });
    }
}

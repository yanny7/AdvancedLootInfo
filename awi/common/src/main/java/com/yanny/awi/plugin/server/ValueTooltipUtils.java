package com.yanny.awi.plugin.server;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.awi.api.IServerUtils;
import com.yanny.awi.language.Lang;
import net.minecraft.core.Vec3i;
import net.minecraft.util.random.Weighted;
import net.minecraft.util.random.WeightedList;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.GeodeBlockSettings;
import net.minecraft.world.level.levelgen.GeodeCrackSettings;
import net.minecraft.world.level.levelgen.GeodeLayerSettings;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.EndSpikeFeature;
import net.minecraft.world.level.levelgen.feature.WeightedPlacedFeature;
import net.minecraft.world.level.levelgen.feature.configurations.BlockColumnConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.TemplateFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.featuresize.FeatureSize;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.rootplacers.AboveRootPlacement;
import net.minecraft.world.level.levelgen.feature.rootplacers.MangroveRootPlacement;
import net.minecraft.world.level.levelgen.feature.rootplacers.RootPlacer;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.RuleBasedStateProvider;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.structure.templatesystem.*;
import net.minecraft.world.level.levelgen.structure.templatesystem.rule.blockentity.RuleBlockEntityModifier;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.NotNull;

public class ValueTooltipUtils {
    @NotNull
    public static TooltipBuilder getEnumTooltip(IServerUtils utils, Enum<?> value) {
        return utils.getEnumTranslation(utils, value);
    }

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
    public static TooltipBuilder getPlacementModifierTooltip(IServerUtils utils, PlacementModifier value) {
        return utils.getPlacementModifierTooltip(utils, value);
    }

    @NotNull
    public static TooltipBuilder getStructureProcessorTooltip(IServerUtils utils, StructureProcessor value) {
        return utils.getStructureProcessorTooltip(utils, value);
    }

    @NotNull
    public static TooltipBuilder getFeatureConfigurationTooltip(IServerUtils utils, FeatureConfiguration value) {
        return utils.getFeatureTooltip(utils, value);
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
                value.getValues().forEach((p) -> c.add(TooltipBuilder.keyValue(p.property().getName(), p.value().toString())));
            });

            b.add(array.build(Lang.Branch.PROPERTIES));
        });
    }

    @NotNull
    public static TooltipBuilder getFluidStateTooltip(IServerUtils utils, FluidState value) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, value.getType()).build(Lang.Value.FLUID));

            TooltipBuilder array = TooltipBuilder.array((c) -> {
                value.getValues().forEach((p) -> c.add(TooltipBuilder.keyValue(p.property().getName(), p.value())));
            });

            b.add(array.build(Lang.Branch.PROPERTIES));
        });
    }

    @NotNull
    public static TooltipBuilder getVec3iTooltip(IServerUtils utils, Vec3i value) {
        if (value.getX() == 0 && value.getY() == 0 && value.getZ() == 0) {
            return TooltipBuilder.empty();
        }

        return utils.getValueTooltip(utils, "[" + value.getX() + "," + value.getY() + "," + value.getZ() + "]");
    }

    @NotNull
    public static TooltipBuilder getWeightedTooltip(IServerUtils utils, Weighted<?> value) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, value.weight()).build(Lang.Value.WEIGHT));
            b.add(utils.getValueTooltip(utils, value.value()).build(Lang.Branch.VALUE));
        });
    }

    @NotNull
    public static TooltipBuilder getBlockColumnConfigurationLayerTooltip(IServerUtils utils, BlockColumnConfiguration.Layer value) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, value.height()).build(Lang.Branch.HEIGHT));
            b.add(utils.getValueTooltip(utils, value.state()).build(Lang.Branch.STATE));
        });
    }

    @NotNull
    public static TooltipBuilder getGeodeBlockSettingsTooltip(IServerUtils utils, GeodeBlockSettings value) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, value.fillingProvider()).build(Lang.Branch.FILLING_PROVIDER));
            b.add(utils.getValueTooltip(utils, value.innerLayerProvider()).build(Lang.Branch.INNER_LAYER_PROVIDER));
            b.add(utils.getValueTooltip(utils, value.alternateInnerLayerProvider()).build(Lang.Branch.ALTERNATE_INNER_LAYER_PROVIDER));
            b.add(utils.getValueTooltip(utils, value.middleLayerProvider()).build(Lang.Branch.MIDDLE_LAYER_PROVIDER));
            b.add(utils.getValueTooltip(utils, value.outerLayerProvider()).build(Lang.Branch.OUTER_LAYER_PROVIDER));
            b.add(utils.getValueTooltip(utils, value.innerPlacements()).build(Lang.Branch.INNER_PLACEMENTS));
            b.add(utils.getValueTooltip(utils, value.cannotReplace()).build(Lang.Branch.CANNOT_REPLACE));
            b.add(utils.getValueTooltip(utils, value.invalidBlocks()).build(Lang.Branch.INVALID_BLOCKS));
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
            b.add(utils.getValueTooltip(utils, value.feature()).build(Lang.Branch.FEATURE));
            b.add(utils.getValueTooltip(utils, value.chance()).build(Lang.Value.CHANCE));
        });
    }

    @NotNull
    public static TooltipBuilder getEndSpikeTooltip(IServerUtils utils, EndSpikeFeature.EndSpike value) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, value.getCenterX()).build(Lang.Value.CENTER_X));
            b.add(utils.getValueTooltip(utils, value.getCenterZ()).build(Lang.Value.CENTER_Z));
            b.add(utils.getValueTooltip(utils, value.getRadius()).build(Lang.Branch.RADIUS));
            b.add(utils.getValueTooltip(utils, value.getHeight()).build(Lang.Branch.HEIGHT));
            b.add(utils.getValueTooltip(utils, value.isGuarded()).build(Lang.Value.IS_GUARDED));
        });
    }

    @NotNull
    public static TooltipBuilder getPlacedFeatureTooltip(IServerUtils utils, PlacedFeature value) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, value.feature()).build(Lang.Branch.FEATURE));
            b.add(utils.getValueTooltip(utils, value.placement()).build(Lang.Branch.PLACEMENT));
        });
    }

    @NotNull
    public static TooltipBuilder getConfiguredFeatureTooltip(IServerUtils utils, ConfiguredFeature<?, ?> value) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, value.feature()).build(Lang.Value.FEATURE));
            b.add(utils.getValueTooltip(utils, value.config()).build(Lang.Branch.CONFIG));
        });
    }

    @NotNull
    public static TooltipBuilder getRuleBasedBlockStateProviderRuleTooltip(IServerUtils utils, RuleBasedStateProvider.Rule value) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, value.ifTrue()).build(Lang.Branch.IF_TRUE));
            b.add(utils.getValueTooltip(utils, value.then()).build(Lang.Branch.THEN));
        });
    }

    @NotNull
    public static TooltipBuilder getStructureProcessorListTooltip(IServerUtils utils, StructureProcessorList value) {
        return utils.getValueTooltip(utils, value.list());
    }

    @NotNull
    public static TooltipBuilder getProcessorRuleTooltip(IServerUtils utils, ProcessorRule value) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, value.inputPredicate).build(Lang.Branch.INPUT_PREDICATE));
            b.add(utils.getValueTooltip(utils, value.locPredicate).build(Lang.Branch.LOCATION_PREDICATE));
            b.add(utils.getValueTooltip(utils, value.posPredicate).build(Lang.Value.POSITION_PREDICATE));
            b.add(utils.getValueTooltip(utils, value.outputState).build(Lang.Branch.OUTPUT_STATE));
            b.add(utils.getValueTooltip(utils, value.blockEntityModifier).build(Lang.Value.BLOCK_ENTITY_MODIFIER));
        });
    }

    @NotNull
    public static TooltipBuilder getMangroveRootPlacementTooltip(IServerUtils utils, MangroveRootPlacement value) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, value.canGrowThrough()).build(Lang.Branch.CAN_GROW_THROUGH));
            b.add(utils.getValueTooltip(utils, value.muddyRootsIn()).build(Lang.Branch.MUDDY_ROOTS_IN));
            b.add(utils.getValueTooltip(utils, value.muddyRootsProvider()).build(Lang.Branch.MUDDY_ROOTS_PROVIDER));
            b.add(utils.getValueTooltip(utils, value.maxRootWidth()).build(Lang.Value.MAX_ROOT_WIDTH));
            b.add(utils.getValueTooltip(utils, value.maxRootLength()).build(Lang.Value.MAX_ROOT_LENGTH));
            b.add(utils.getValueTooltip(utils, value.randomSkewChance()).build(Lang.Value.RANDOM_SKEW_CHANCE));
        });
    }

    @NotNull
    public static TooltipBuilder getAboveRootPlacementTooltip(IServerUtils utils, AboveRootPlacement value) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, value.aboveRootProvider()).build(Lang.Branch.ABOVE_ROOT_PROVIDER));
            b.add(utils.getValueTooltip(utils, value.aboveRootPlacementChance()).build(Lang.Value.PLACEMENT_CHANCE));
        });
    }

    @NotNull
    public static TooltipBuilder getWeightedListTooltip(IServerUtils utils, WeightedList<?> value) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, value.totalWeight).build(Lang.Value.TOTAL_WEIGHT));
            b.add(utils.getValueTooltip(utils, value.unwrap()).build(Lang.Branch.ITEMS));
        });
    }

    @NotNull
    public static TooltipBuilder getPosRuleTestTooltip(IServerUtils utils, PosRuleTest value) {
        return utils.getValueTooltip(utils, value.getType());
    }

    @NotNull
    public static TooltipBuilder getRuleBlockEntityModifierTooltip(IServerUtils utils, RuleBlockEntityModifier value) {
        return utils.getValueTooltip(utils, value.getType());
    }

    @NotNull
    public static TooltipBuilder getTemplateEntryTooltip(IServerUtils utils, TemplateFeatureConfiguration.TemplateEntry value) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, value.template()).build(Lang.Value.TEMPLATE));
            b.add(utils.getValueTooltip(utils, value.rotations()).build(Lang.Branch.ROTATIONS));
        });
    }
}

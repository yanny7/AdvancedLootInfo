package com.yanny.awi.plugin.common.nodes;

import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.awi.Utils;
import com.yanny.awi.api.IServerUtils;
import com.yanny.awi.api.ListNode;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class PlacedFeatureNode extends ListNode {
    public static final ResourceLocation ID = Utils.modLoc("placed_feature");

    public PlacedFeatureNode(IServerUtils utils, PlacedFeature placedFeature) {
        // -> PlacedFeatureNode
        //   -> ConfiguredFeature:
        //     -> FeatureConfiguration (items)
        //     -> Feature
        //   -> Placement: (conditions)

        ConfiguredFeature<?, ?> configuredFeature = placedFeature.feature().value();

        FeatureConfiguration featureConfiguration = configuredFeature.config(); // values

//        List<Component> components = CoreTooltipUtils.toComponents(utils.getFeatureTooltip(utils, featureConfiguration).build(), 0, false);

//        for (Component component : components) {
//            System.out.println(component.toString());
//        }

        Set<Block> blocks = new HashSet<>();
        Iterator<BlockPos> posIterable = BlockPos.randomInCube(utils.getServerLevel().getRandom(), 128, BlockPos.ZERO, 128).iterator();

        try {
            for (int i = 0; i < 5; i++) {
                configuredFeature.place(
                        new FakeWorldGenLevel(utils.getServerLevel(), blocks),
                        new FakeChunkGenerator(utils.getServerLevel()),
                        utils.getServerLevel().getRandom(),
                        posIterable.next()
                );
            }
        } catch (Throwable e) {
            e.printStackTrace();
            System.out.println("Failed to place");
        } finally {
            blocks.addAll(utils.getItemCollector(utils, configuredFeature));
        }

        for (Block block : blocks) {
            addChildren(new BlockNode(utils, block));
        }

        for (PlacementModifier placementModifier : placedFeature.placement()) {
            // conditions
        }
    }

    @Override
    public void encodeNode(IServerUtils utils, FriendlyByteBuf buf) {

    }

    @NotNull
    @Override
    public TooltipNode getTooltip() {
        return null;
    }

    @NotNull
    @Override
    public ResourceLocation getId() {
        return ID;
    }
}

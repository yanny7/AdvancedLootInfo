package com.yanny.awi.plugin.common.nodes;

import com.mojang.logging.LogUtils;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.awi.Utils;
import com.yanny.awi.api.IClientUtils;
import com.yanny.awi.api.IServerUtils;
import com.yanny.awi.api.ListNode;
import com.yanny.awi.language.Lang;
import com.yanny.awi.plugin.server.summary.ColumnContext;
import net.minecraft.core.Holder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class LevelStemNode extends ListNode {
    public static final ResourceLocation ID = Utils.modLoc("level_stem");

    private static final Logger LOGGER = LogUtils.getLogger();

    public LevelStemNode(IServerUtils utils, LevelStem levelStem, Map<Holder<Biome>, NodeUtils.LayerHolder> baseLayouts,
                         WorldgenNodeCache nodeCache) {
        ChunkGenerator generator = levelStem.generator();
        ColumnContext columnContext = new ColumnContext(generator.getMinY(), generator.getGenDepth());
        Block defaultBlock = Blocks.AIR;
        Fluid defaultFluid = Fluids.EMPTY;
        int seaLevel = generator.getSeaLevel();

        if (generator instanceof NoiseBasedChunkGenerator g) {
            NoiseGeneratorSettings settings = g.generatorSettings().value();
            defaultBlock = settings.defaultBlock().getBlock();
            defaultFluid = settings.defaultFluid().getFluidState().getType();
        }

        TooltipNode tooltip = buildTooltip(utils, defaultBlock, defaultFluid, seaLevel);

        for (Holder<Biome> biomeHolder : generator.getBiomeSource().possibleBiomes()) {
            NodeUtils.LayerHolder layers = baseLayouts.get(biomeHolder);
            Set<NodeUtils.BlockInfo> baseBlocks = layers != null ? layers.getBlockInfos() : Collections.emptySet();

            try {
                addChildren(new BiomeNode(utils, biomeHolder.value(), tooltip, baseBlocks, columnContext, nodeCache));
            } catch (Exception e) {
                LOGGER.error("Failed to analyze biome {}", biomeName(biomeHolder), e);
            }
        }
    }

    public LevelStemNode(IClientUtils utils, FriendlyByteBuf buf) {
        super(utils, buf);
    }

    @Override
    public void encodeNode(IServerUtils utils, FriendlyByteBuf buf) {
    }

    @NotNull
    @Override
    public TooltipNode getTooltip() {
        return TooltipNode.empty();
    }

    @NotNull
    @Override
    public ResourceLocation getId() {
        return ID;
    }

    private TooltipNode buildTooltip(IServerUtils utils, Block defaultBlock, Fluid defaultFluid, int seaLevel) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, defaultBlock).build(Lang.Value.DEFAULT_BLOCK));

            if (!defaultFluid.isSame(Fluids.EMPTY)) {
                b.add(utils.getValueTooltip(utils, defaultFluid).build(Lang.Value.DEFAULT_FLUID));
                b.add(utils.getValueTooltip(utils, seaLevel).build(Lang.Value.SEA_LEVEL));
            }
        }).build();
    }

    private static String biomeName(Holder<Biome> biomeHolder) {
        return biomeHolder.unwrapKey().map(k -> k.location().toString()).orElse("<unnamed biome>");
    }
}
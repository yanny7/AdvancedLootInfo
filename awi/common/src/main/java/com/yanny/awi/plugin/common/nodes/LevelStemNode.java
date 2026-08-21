package com.yanny.awi.plugin.common.nodes;

import com.yanny.aci.CommonLogUtils;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.awi.Utils;
import com.yanny.awi.api.IClientUtils;
import com.yanny.awi.api.IServerUtils;
import com.yanny.awi.api.ListNode;
import com.yanny.awi.language.Lang;
import com.yanny.awi.plugin.server.summary.ColumnContext;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
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
    public static final Identifier ID = Utils.modLoc("level_stem");

    private static final Logger LOGGER = CommonLogUtils.getLogger(Utils.MOD_ID);

    private final TooltipNode tooltip;

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

        TooltipNode biomeTooltip = buildTooltip(utils, defaultFluid, seaLevel);

        for (Holder<Biome> biomeHolder : generator.getBiomeSource().possibleBiomes()) {
            NodeUtils.LayerHolder layers = baseLayouts.get(biomeHolder);
            Set<NodeUtils.BlockInfo> baseBlocks = layers != null ? layers.getBlockInfos() : Collections.emptySet();

            try {
                addChildren(new BiomeNode(utils, biomeHolder.value(), biomeTooltip, baseBlocks, defaultBlock, defaultFluid, columnContext, nodeCache));
            } catch (Exception e) {
                LOGGER.error("Failed to analyze biome {}", biomeName(biomeHolder), e);
            }
        }

        tooltip = TooltipNode.empty();
    }

    public LevelStemNode(IClientUtils utils, RegistryFriendlyByteBuf buf) {
        super(utils, buf);
        tooltip = utils.getTooltipCache().getNodeById(buf.readVarInt());
    }

    @Override
    public void encodeNode(IServerUtils utils, RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(utils.getTooltipCache().getNodeId(tooltip));
    }

    @NotNull
    @Override
    public TooltipNode getTooltip() {
        return tooltip;
    }

    @NotNull
    @Override
    public Identifier getId() {
        return ID;
    }

    private TooltipNode buildTooltip(IServerUtils utils, Fluid defaultFluid, int seaLevel) {
        return TooltipBuilder.array((b) -> {
            if (!defaultFluid.isSame(Fluids.EMPTY)) {
                b.add(utils.getValueTooltip(utils, seaLevel).build(Lang.Value.SEA_LEVEL));
            }
        }).build();
    }

    private static String biomeName(Holder<Biome> biomeHolder) {
        return biomeHolder.unwrapKey().map(k -> k.identifier().toString()).orElse("<unnamed biome>");
    }
}
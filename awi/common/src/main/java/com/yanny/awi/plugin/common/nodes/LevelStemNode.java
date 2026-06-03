package com.yanny.awi.plugin.common.nodes;

import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.awi.Utils;
import com.yanny.awi.api.IClientUtils;
import com.yanny.awi.api.IServerUtils;
import com.yanny.awi.api.ListNode;
import net.minecraft.core.Holder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import org.jetbrains.annotations.NotNull;

public class LevelStemNode extends ListNode {
    public static final ResourceLocation ID = Utils.modLoc("level_stem");

    private final TooltipNode tooltip;

    public LevelStemNode(IServerUtils utils, LevelStem levelStem) {
        ChunkGenerator generator = levelStem.generator();
        int seaLevel;
        Block defaultBlock;

        if (generator instanceof NoiseBasedChunkGenerator g) {
            defaultBlock = g.generatorSettings().value().defaultBlock().getBlock();
        } else {
            defaultBlock = Blocks.AIR;
        }

        seaLevel = generator.getSeaLevel();
        tooltip = TooltipNode.empty();

        for (Holder<Biome> biomeHolder : generator.getBiomeSource().possibleBiomes()) {
            addChildren(new BiomeNode(utils, biomeHolder.value()));
        }
    }

    public LevelStemNode(IClientUtils utils, FriendlyByteBuf buf) {
        super(utils, buf);
        tooltip = utils.getTooltipCache().getNodeById(buf.readVarInt());
    }

    @Override
    public void encodeNode(IServerUtils utils, FriendlyByteBuf buf) {
        buf.writeVarInt(utils.getTooltipCache().getNodeId(tooltip));
    }

    @NotNull
    @Override
    public TooltipNode getTooltip() {
        return tooltip;
    }

    @NotNull
    @Override
    public ResourceLocation getId() {
        return ID;
    }
}

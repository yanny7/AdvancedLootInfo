package com.yanny.awi.plugin.common.nodes;

import com.yanny.awi.Utils;
import com.yanny.awi.api.IServerUtils;
import com.yanny.awi.api.ITooltipNode;
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

    private final int seaLevel;
    private final Block defaultBlock;

    public LevelStemNode(IServerUtils utils, LevelStem levelStem) {
        ChunkGenerator generator = levelStem.generator();

        if (generator instanceof NoiseBasedChunkGenerator g) {
            defaultBlock = g.generatorSettings().value().defaultBlock().getBlock();
        } else {
            defaultBlock = Blocks.AIR;
        }

        seaLevel = generator.getSeaLevel();

        for (Holder<Biome> biomeHolder : generator.getBiomeSource().possibleBiomes()) {
            addChildren(new BiomeNode(utils, biomeHolder.value()));
        }
    }

    @Override
    public void encodeNode(IServerUtils utils, FriendlyByteBuf buf) {

    }

    @NotNull
    @Override
    public ITooltipNode getTooltip() {
        return null;
    }

    @NotNull
    @Override
    public ResourceLocation getId() {
        return ID;
    }
}

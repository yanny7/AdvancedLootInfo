package com.yanny.awi.plugin.common.nodes;

import com.yanny.awi.Utils;
import com.yanny.awi.api.IServerUtils;
import com.yanny.awi.api.ITooltipNode;
import com.yanny.awi.api.ListNode;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;

public class LevelStemNode extends ListNode {
    public static final Identifier ID = Utils.modLoc("level_stem");

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
    public void encodeNode(IServerUtils utils, RegistryFriendlyByteBuf buf) {

    }

    @Override
    public ITooltipNode getTooltip() {
        return null;
    }

    @Override
    public Identifier getId() {
        return ID;
    }
}

package com.yanny.awi.plugin.common.nodes;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.awi.Utils;
import com.yanny.awi.api.IClientUtils;
import com.yanny.awi.api.IServerUtils;
import com.yanny.awi.api.ListNode;
import com.yanny.awi.language.Lang;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class LevelStemNode extends ListNode {
    public static final ResourceLocation ID = Utils.modLoc("level_stem");

    public LevelStemNode(IServerUtils utils, LevelStem levelStem) {
        ChunkGenerator generator = levelStem.generator();
        int seaLevel;
        Block defaultBlock;
        Fluid defaultFluid;

        if (generator instanceof NoiseBasedChunkGenerator g) {
            NoiseGeneratorSettings settings = g.generatorSettings().value();
            defaultBlock = settings.defaultBlock().getBlock();
            defaultFluid = settings.defaultFluid().getFluidState().getType();
        } else {
            defaultBlock = Blocks.AIR;
            defaultFluid = Fluids.EMPTY;
        }

        seaLevel = generator.getSeaLevel();

        for (Holder<Biome> biomeHolder : generator.getBiomeSource().possibleBiomes()) {
            TooltipNode tooltip = TooltipBuilder.array((b) -> {
                b.add(utils.getValueTooltip(utils, defaultBlock).build(Lang.Value.DEFAULT_BLOCK));

                if (!defaultFluid.isSame(Fluids.EMPTY)) {
                    b.add(utils.getValueTooltip(utils, defaultFluid).build(Lang.Value.DEFAULT_FLUID));
                    b.add(utils.getValueTooltip(utils, seaLevel).build(Lang.Value.SEA_LEVEL));
                }
            }).build();

            ServerLevel serverLevel = utils.getServerLevel();
            Set<Block> blocks;

            if (levelStem.generator() instanceof NoiseBasedChunkGenerator noiseGenerator) {
                RegistryAccess registryAccess = serverLevel.registryAccess();
                RandomState randomState = RandomState.create(noiseGenerator.generatorSettings().value(), registryAccess.lookupOrThrow(Registries.NOISE), serverLevel.getSeed());
                blocks = NodeUtils.mineSurfaceBlocksForBiome(registryAccess, noiseGenerator, randomState, biomeHolder);
            } else {
                blocks = new HashSet<>();
            }

            addChildren(new BiomeNode(utils, biomeHolder.value(), tooltip, blocks));
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
}

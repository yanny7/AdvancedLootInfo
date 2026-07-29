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
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
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
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Supplier;

public class LevelStemNode extends ListNode {
    public static final ResourceLocation ID = Utils.modLoc("level_stem");

    private static final Logger LOGGER = LogUtils.getLogger();

    public LevelStemNode(IServerUtils utils, LevelStem levelStem) {
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

        if (generator instanceof NoiseBasedChunkGenerator noiseGenerator) {
            processNoiseGenerator(utils, noiseGenerator, generator, tooltip, columnContext);
        } else {
            processDefaultGenerator(utils, generator, tooltip, columnContext);
        }
    }

    private void processDefaultGenerator(IServerUtils utils, ChunkGenerator generator, TooltipNode tooltip, ColumnContext columnContext) {
        for (Holder<Biome> biomeHolder : generator.getBiomeSource().possibleBiomes()) {
            addChildren(new BiomeNode(utils, biomeHolder.value(), tooltip, Collections.emptySet(), columnContext));
        }
    }

    public LevelStemNode(IClientUtils utils, RegistryFriendlyByteBuf buf) {
        super(utils, buf);
    }

    @Override
    public void encodeNode(IServerUtils utils, RegistryFriendlyByteBuf buf) {
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

    private void processNoiseGenerator(IServerUtils utils, NoiseBasedChunkGenerator noiseGenerator, ChunkGenerator generator, TooltipNode tooltip, ColumnContext columnContext) {
        ServerLevel serverLevel = utils.getServerLevel();
        RegistryAccess registryAccess = serverLevel.registryAccess();
        Supplier<NodeUtils.DimensionContext> contextFactory = () -> {
            RandomState localRandomState = RandomState.create(
                    noiseGenerator.generatorSettings().value(),
                    registryAccess.lookupOrThrow(Registries.NOISE),
                    serverLevel.getSeed()
            );
            return new NodeUtils.DimensionContext(registryAccess, noiseGenerator, localRandomState);
        };
        ThreadLocal<NodeUtils.DimensionContext> threadLocalCtx = ThreadLocal.withInitial(contextFactory);
        List<Holder<Biome>> biomes = new ArrayList<>(generator.getBiomeSource().possibleBiomes());
        int threadCount = Math.max(1, Runtime.getRuntime().availableProcessors() - 1);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        try {
            List<Future<BiomeResult>> futures = biomes.stream()
                    .map(biomeHolder -> executor.submit(() -> {
                        NodeUtils.DimensionContext ctx = threadLocalCtx.get();
                        NodeUtils.LayerHolder result = NodeUtils.getBaseBlocksForBiome(ctx, biomeHolder);
                        return new BiomeResult(biomeHolder, result);
                    }))
                    .toList();

            for (Future<BiomeResult> future : futures) {
                try {
                    BiomeResult res = future.get();
                    addChildren(new BiomeNode(utils, res.biomeHolder().value(), tooltip, res.layers.getBlockInfos(), columnContext));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    LOGGER.error("Biome analysis interrupted", e);
                } catch (Exception e) {
                    LOGGER.error("Failed to analyze biome", e);
                }
            }
        } finally {
            executor.shutdown();
            threadLocalCtx.remove();
        }
    }

    record BiomeResult(Holder<Biome> biomeHolder, NodeUtils.LayerHolder layers) {}
}
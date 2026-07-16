package com.yanny.awi.plugin.common.nodes;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.attribute.EnvironmentAttributeReader;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.ticks.LevelTickAccess;
import net.minecraft.world.ticks.TickPriority;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public class FakeWorldGenLevel implements WorldGenLevel {
    private final ServerLevel serverLevel;
    private final Set<Block> blocks;
    private final FakeChunkAccess fakeChunkAccess;

    public FakeWorldGenLevel(ServerLevel serverLevel, Set<Block> blocks) {
        this.serverLevel = serverLevel;
        this.blocks = blocks;
        fakeChunkAccess = new FakeChunkAccess(this, serverLevel, blocks);
    }

    @Override
    public long getSeed() {
        return serverLevel.getSeed();
    }

    @NotNull
    @Override
    public ServerLevel getLevel() {
        throw new IllegalStateException();
    }

    @Override
    public long nextSubTickCount() {
        return serverLevel.nextSubTickCount();
    }

    @NotNull
    @Override
    public LevelTickAccess<Block> getBlockTicks() {
        throw new IllegalStateException();
    }

    @NotNull
    @Override
    public LevelTickAccess<Fluid> getFluidTicks() {
        throw new IllegalStateException();
    }

    @NotNull
    @Override
    public LevelData getLevelData() {
        throw new IllegalStateException();
    }

    @NotNull
    @Override
    public DifficultyInstance getCurrentDifficultyAt(BlockPos blockPos) {
        return serverLevel.getCurrentDifficultyAt(blockPos);
    }

    @Nullable
    @Override
    public MinecraftServer getServer() {
        return null;
    }

    @NotNull
    @Override
    public ChunkSource getChunkSource() {
        throw new IllegalStateException();
    }

    @NotNull
    @Override
    public RandomSource getRandom() {
        return serverLevel.getRandom();
    }

    @Override
    public void playSound(@Nullable Entity entity, BlockPos blockPos, SoundEvent soundEvent, SoundSource soundSource, float f, float g) {

    }

    @Override
    public void addParticle(ParticleOptions particleOptions, double d, double e, double f, double g, double h, double i) {

    }

    @Override
    public void levelEvent(@Nullable Entity entity, int i, BlockPos blockPos, int j) {

    }

    @Override
    public void gameEvent(Holder<GameEvent> holder, Vec3 vec3, GameEvent.Context context) {

    }

    @Override
    public float getShade(Direction direction, boolean bl) {
        return serverLevel.getShade(direction, bl);
    }

    @NotNull
    @Override
    public LevelLightEngine getLightEngine() {
//        throw new IllegalStateException();
        return serverLevel.getLightEngine(); //TODO REMOVE!!!!!!!!!!!!!!!
    }

    @NotNull
    @Override
    public WorldBorder getWorldBorder() {
        throw new IllegalStateException();
    }

    @Nullable
    @Override
    public BlockEntity getBlockEntity(BlockPos blockPos) {
        return null;
    }

    @NotNull
    @Override
    public BlockState getBlockState(BlockPos blockPos) {
        int y = blockPos.getY();

        // 1. Give features an "Air" sky pocket to grow into
        if (y > 64) {
            return Blocks.AIR.defaultBlockState();
        }

        // 2. Give vegetation/structures a valid surface to spawn on
        if (y == 64) {
            // Return Grass/Dirt so trees, flowers, and wells think they are on the surface
            return Blocks.GRASS_BLOCK.defaultBlockState();
        }

        // 3. Provide valid stone/deepslate layers for ore replacement checks
        if (y < 0) {
            return Blocks.DEEPSLATE.defaultBlockState();
        }

        return Blocks.STONE.defaultBlockState();
//        return Blocks.STONE.defaultBlockState(); //TODO random pool?
    }

    @NotNull
    @Override
    public FluidState getFluidState(BlockPos blockPos) {
        return Fluids.EMPTY.defaultFluidState(); // TODO random pool?
    }

    @NotNull
    @Override
    public List<Entity> getEntities(@Nullable Entity entity, AABB aABB, Predicate<? super Entity> predicate) {
        return List.of();
    }

    @NotNull
    @Override
    public <T extends Entity> List<T> getEntities(EntityTypeTest<Entity, T> entityTypeTest, AABB aABB, Predicate<? super T> predicate) {
        return List.of();
    }

    @NotNull
    @Override
    public List<? extends Player> players() {
        return List.of();
    }

    @Nullable
    @Override
    public ChunkAccess getChunk(int i, int j, ChunkStatus chunkStatus, boolean bl) {
        return fakeChunkAccess;
    }

    @Override
    public int getHeight(Heightmap.Types types, int i, int j) {
        return serverLevel.random.nextInt(-128, 128); // TODO
    }

    @Override
    public int getSkyDarken() {
        return serverLevel.getSkyDarken();
    }

    @NotNull
    @Override
    public BiomeManager getBiomeManager() {
        return serverLevel.getBiomeManager();
    }

    @NotNull
    @Override
    public Holder<Biome> getUncachedNoiseBiome(int i, int j, int k) {
        return serverLevel.getUncachedNoiseBiome(i, j, k);
    }

    @Override
    public boolean isClientSide() {
        return serverLevel.isClientSide();
    }

    @Override
    public int getSeaLevel() {
        //TODO based on loaded data!
        return serverLevel.getSeaLevel();
    }

    @NotNull
    @Override
    public DimensionType dimensionType() {
        //TODO based on loaded data!
        return serverLevel.dimensionType();
    }

    @NotNull
    @Override
    public RegistryAccess registryAccess() {
        return serverLevel.registryAccess();
    }

    @NotNull
    @Override
    public FeatureFlagSet enabledFeatures() {
        return serverLevel.enabledFeatures();
    }

    @NotNull
    @Override
    public EnvironmentAttributeReader environmentAttributes() {
        return serverLevel.environmentAttributes();
    }

    @Override
    public boolean isStateAtPosition(BlockPos blockPos, Predicate<BlockState> predicate) {
        //TODO random pool?
        return serverLevel.isStateAtPosition(blockPos, predicate);
    }

    @Override
    public boolean isFluidAtPosition(BlockPos blockPos, Predicate<FluidState> predicate) {
        //TODO random pool?
        return serverLevel.isFluidAtPosition(blockPos, predicate);
    }

    @Override
    public boolean setBlock(BlockPos blockPos, BlockState blockState, int i, int j) {
        blocks.add(blockState.getBlock());
        return true;
    }

    @Override
    public boolean removeBlock(BlockPos blockPos, boolean bl) {
        return true;
    }

    @Override
    public boolean destroyBlock(BlockPos blockPos, boolean bl, @Nullable Entity entity, int i) {
        return true;
    }

    /*
     * REMOVED OVERRIDES
     */

    @Override
    public void scheduleTick(BlockPos blockPos, Block block, int i) {

    }

    @Override
    public void scheduleTick(BlockPos blockPos, Fluid fluid, int i) {

    }

    @Override
    public void scheduleTick(BlockPos blockPos, Block block, int i, TickPriority tickPriority) {

    }

    @Override
    public void scheduleTick(BlockPos blockPos, Fluid fluid, int i, TickPriority tickPriority) {

    }

    @Override
    public void neighborShapeChanged(Direction direction, BlockPos blockPos, BlockPos blockPos2, BlockState blockState, int i, int j) {

    }
}

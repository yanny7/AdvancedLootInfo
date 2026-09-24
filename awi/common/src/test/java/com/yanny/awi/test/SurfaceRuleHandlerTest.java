package com.yanny.awi.test;

import com.google.gson.JsonObject;
import com.yanny.aci.api.RangeValue;
import com.yanny.awi.api.BlockInfo;
import com.yanny.awi.api.ISurfaceRuleHandler;
import com.yanny.awi.plugin.common.nodes.BandlandsLayout;
import com.yanny.awi.plugin.common.nodes.NodeUtils;
import com.yanny.awi.test.utils.BaseLayoutTestUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.PalettedContainerFactory;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SurfaceRuleHandlerTest {
    private static final long SEED = 1234L;

    @BeforeAll
    static void setUp() {
        BaseLayoutTestUtils.bootstrap();
    }

    @Test
    public void testHandlerGetsDefinitionAndGhostWindow() {
        List<JsonObject> definitions = new ArrayList<>();
        ISurfaceRuleHandler handler = (definition, ghost) -> {
            definitions.add(definition);
            return List.of(new BlockInfo(Blocks.DIAMOND_BLOCK, BlockInfo.StorageType.ABSOLUTE,
                    List.of(new RangeValue(ghost.absoluteY().first(), ghost.absoluteY().last())), 0, ghost.water(), ghost.placement(), List.of()));
        };
        Set<BlockInfo> infos = scan(Map.of(BandlandsLayout.TYPE, (context) -> handler));
        BlockInfo diamond = infos.stream().filter((info) -> info.block() == Blocks.DIAMOND_BLOCK).findFirst().orElseThrow();

        assertEquals("{\"type\":\"minecraft:bandlands\"}", definitions.get(0).toString());
        assertEquals("54-318", diamond.ranges().get(0).toIntString());
        assertTrue(infos.stream().noneMatch((info) -> info.block() == Blocks.LIGHT), "a ghost leaked into the result");
        assertTrue(infos.stream().noneMatch((info) -> info.block() == Blocks.YELLOW_TERRACOTTA), "bands still measured");
    }

    @Test
    public void testUnavailableHandlerLeavesTheRuleMeasured() {
        assertMeasured(scan(Map.of(BandlandsLayout.TYPE, (context) -> null)));
    }

    @Test
    public void testFailingHandlerLeavesTheRuleMeasured() {
        assertMeasured(scan(Map.of(BandlandsLayout.TYPE, (context) -> {
            throw new IllegalStateException("handler failed to start");
        })));
    }

    @Test
    public void testThrowingExpandKeepsTheRestOfTheBiome() {
        Set<BlockInfo> infos = scan(Map.of(BandlandsLayout.TYPE, (context) -> (definition, ghost) -> {
            throw new IllegalStateException("expand failed");
        }));

        assertTrue(infos.stream().anyMatch((info) -> info.block() == Blocks.RED_SAND));
        assertTrue(infos.stream().noneMatch((info) -> info.block() == Blocks.LIGHT), "a ghost leaked into the result");
    }

    private static void assertMeasured(Set<BlockInfo> infos) {
        assertTrue(infos.stream().anyMatch((info) -> info.block() == Blocks.YELLOW_TERRACOTTA && info.layerShift() == 0));
        assertTrue(infos.stream().noneMatch((info) -> info.layerShift() > 0));
    }

    private static Set<BlockInfo> scan(Map<Identifier, Function<ISurfaceRuleHandler.Context, ISurfaceRuleHandler>> factories) {
        NoiseBasedChunkGenerator generator = (NoiseBasedChunkGenerator) BaseLayoutTestUtils.levelStems().getValueOrThrow(LevelStem.OVERWORLD).generator();
        RandomState randomState = RandomState.create(generator.generatorSettings().value(),
                BaseLayoutTestUtils.registryAccess().lookupOrThrow(Registries.NOISE), SEED);
        NodeUtils.DimensionContext context = new NodeUtils.DimensionContext(BaseLayoutTestUtils.registryAccess(),
                PalettedContainerFactory.create(BaseLayoutTestUtils.registryAccess()), BaseLayoutTestUtils.lookup(), generator, randomState, factories);
        Holder<Biome> biome = BaseLayoutTestUtils.registryAccess().lookupOrThrow(Registries.BIOME).getOrThrow(Biomes.BADLANDS);

        return NodeUtils.getBaseBlocksForBiome(context, biome, NodeUtils.ScanOptions.DEFAULT).getBlockInfos();
    }
}

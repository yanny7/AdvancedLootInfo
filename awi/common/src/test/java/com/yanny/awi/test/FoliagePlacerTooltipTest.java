package com.yanny.awi.test;

import com.yanny.awi.plugin.server.FoliagePlacerTooltipUtils;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.level.levelgen.feature.foliageplacers.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.yanny.awi.test.TooltipTestSuite.UTILS;
import static com.yanny.awi.test.utils.TestUtils.assertTooltip;

public class FoliagePlacerTooltipTest {
    @Test
    public void testBlobFoliagePlacerTooltip() {
        assertTooltip(FoliagePlacerTooltipUtils.getBlobFoliagePlacerTooltip(UTILS, new BlobFoliagePlacer(ConstantInt.of(2), ConstantInt.of(0), 3)).build(), List.of(
                "Blob:",
                "  -> Radius:",
                "    -> Constant:",
                "      -> Value: 2",
                "  -> Offset:",
                "    -> Constant:",
                "      -> Value: 0",
                "  -> Height: 3"
        ));
    }

    @Test
    public void testSpruceFoliagePlacerTooltip() {
        assertTooltip(FoliagePlacerTooltipUtils.getSpruceFoliagePlacerTooltip(UTILS, new SpruceFoliagePlacer(ConstantInt.of(2), ConstantInt.of(0), ConstantInt.of(4))).build(), List.of(
                "Spruce:",
                "  -> Radius:",
                "    -> Constant:",
                "      -> Value: 2",
                "  -> Offset:",
                "    -> Constant:",
                "      -> Value: 0",
                "  -> Trunk Height:",
                "    -> Constant:",
                "      -> Value: 4"
        ));
    }

    @Test
    public void testPineFoliagePlacerTooltip() {
        assertTooltip(FoliagePlacerTooltipUtils.getPineFoliagePlacerTooltip(UTILS, new PineFoliagePlacer(ConstantInt.of(2), ConstantInt.of(0), ConstantInt.of(4))).build(), List.of(
                "Pine:",
                "  -> Radius:",
                "    -> Constant:",
                "      -> Value: 2",
                "  -> Offset:",
                "    -> Constant:",
                "      -> Value: 0",
                "  -> Height:",
                "    -> Constant:",
                "      -> Value: 4"
        ));
    }

    @Test
    public void testAcaciaFoliagePlacerTooltip() {
        assertTooltip(FoliagePlacerTooltipUtils.getAcaciaFoliagePlacerTooltip(UTILS, new AcaciaFoliagePlacer(ConstantInt.of(2), ConstantInt.of(0))).build(), List.of(
                "Acacia:",
                "  -> Radius:",
                "    -> Constant:",
                "      -> Value: 2",
                "  -> Offset:",
                "    -> Constant:",
                "      -> Value: 0"
        ));
    }

    @Test
    public void testBushFoliagePlacerTooltip() {
        assertTooltip(FoliagePlacerTooltipUtils.getBushFoliagePlacerTooltip(UTILS, new BushFoliagePlacer(ConstantInt.of(2), ConstantInt.of(0), 2)).build(), List.of(
                "Bush:",
                "  -> Radius:",
                "    -> Constant:",
                "      -> Value: 2",
                "  -> Offset:",
                "    -> Constant:",
                "      -> Value: 0",
                "  -> Height: 2"
        ));
    }

    @Test
    public void testFancyFoliagePlacerTooltip() {
        assertTooltip(FoliagePlacerTooltipUtils.getFancyFoliagePlacerTooltip(UTILS, new FancyFoliagePlacer(ConstantInt.of(2), ConstantInt.of(0), 4)).build(), List.of(
                "Fancy:",
                "  -> Radius:",
                "    -> Constant:",
                "      -> Value: 2",
                "  -> Offset:",
                "    -> Constant:",
                "      -> Value: 0",
                "  -> Height: 4"
        ));
    }

    @Test
    public void testMegaJungleFoliagePlacerTooltip() {
        assertTooltip(FoliagePlacerTooltipUtils.getMegaJungleFoliagePlacerTooltip(UTILS, new MegaJungleFoliagePlacer(ConstantInt.of(2), ConstantInt.of(0), 2)).build(), List.of(
                "Mega Jungle:",
                "  -> Radius:",
                "    -> Constant:",
                "      -> Value: 2",
                "  -> Offset:",
                "    -> Constant:",
                "      -> Value: 0",
                "  -> Height: 2"
        ));
    }

    @Test
    public void testMegaPineFoliagePlacerTooltip() {
        assertTooltip(FoliagePlacerTooltipUtils.getMegaPineFoliagePlacerTooltip(UTILS, new MegaPineFoliagePlacer(ConstantInt.of(2), ConstantInt.of(0), ConstantInt.of(13))).build(), List.of(
                "Mega Pine:",
                "  -> Radius:",
                "    -> Constant:",
                "      -> Value: 2",
                "  -> Offset:",
                "    -> Constant:",
                "      -> Value: 0",
                "  -> Crown Height:",
                "    -> Constant:",
                "      -> Value: 13"
        ));
    }

    @Test
    public void testDarkOakFoliagePlacerTooltip() {
        assertTooltip(FoliagePlacerTooltipUtils.getDarkOakFoliagePlacerTooltip(UTILS, new DarkOakFoliagePlacer(ConstantInt.of(2), ConstantInt.of(0))).build(), List.of(
                "Dark Oak:",
                "  -> Radius:",
                "    -> Constant:",
                "      -> Value: 2",
                "  -> Offset:",
                "    -> Constant:",
                "      -> Value: 0"
        ));
    }

    @Test
    public void testRandomSpreadFoliagePlacerTooltip() {
        assertTooltip(FoliagePlacerTooltipUtils.getRandomSpreadFoliagePlacerTooltip(UTILS, new RandomSpreadFoliagePlacer(ConstantInt.of(2), ConstantInt.of(0), ConstantInt.of(50), 32)).build(), List.of(
                "Random Spread:",
                "  -> Radius:",
                "    -> Constant:",
                "      -> Value: 2",
                "  -> Offset:",
                "    -> Constant:",
                "      -> Value: 0",
                "  -> Foliage Height:",
                "    -> Constant:",
                "      -> Value: 50",
                "  -> Leaf Placement Attempts: 32"
        ));
    }

    @Test
    public void testCherryFoliagePlacerTooltip() {
        assertTooltip(FoliagePlacerTooltipUtils.getCherryFoliagePlacerTooltip(UTILS, new CherryFoliagePlacer(
                ConstantInt.of(2),
                ConstantInt.of(0),
                ConstantInt.of(5),
                0.2f,
                0.3f,
                0.1f,
                0.05f
        )).build(), List.of(
                "Cherry:",
                "  -> Radius:",
                "    -> Constant:",
                "      -> Value: 2",
                "  -> Offset:",
                "    -> Constant:",
                "      -> Value: 0",
                "  -> Height:",
                "    -> Constant:",
                "      -> Value: 5",
                "  -> Wide Bottom Layer Hole Chance: 0.2",
                "  -> Corner Hole Chance: 0.3",
                "  -> Hanging Leaves Chance: 0.1",
                "  -> Hanging Leaves Extension Chance: 0.05"
        ));
    }
}

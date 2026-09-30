package com.yanny.aci.test;

import com.yanny.aci.api.NumberExpr;
import com.yanny.aci.api.NumberFunction;
import com.yanny.aci.api.NumberFunctions;
import com.yanny.aci.api.NumberInterval;
import com.yanny.aci.language.CoreLang;
import com.yanny.aci.manager.ManagedRegistry;
import com.yanny.aci.manager.NumberConverters;
import com.yanny.aci.number.NumberFormatter;
import com.yanny.aci.test.utils.TestClientUtils;
import com.yanny.aci.test.utils.TestServerUtils;
import com.yanny.aci.test.utils.TestUtils;
import com.yanny.aci.tooltip.*;
import io.netty.buffer.Unpooled;
import net.minecraft.locale.Language;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.StringDecomposer;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

import static com.yanny.aci.api.NumberExpr.*;
import static org.junit.jupiter.api.Assertions.*;

public class NumberTooltipTest {
    private static final String MOD_ID = "aci_test";
    private static final String COUNT = "test.count";
    private static final String CONDITION = "test.condition";
    private static final Var LOOTING = level("minecraft:looting", 3);
    private static final Var FORTUNE = level("minecraft:fortune", 3);
    private static final ResourceLocation BOOM = new ResourceLocation("aci_test", "boom");
    private static final NumberOptions TEXT = NumberOptions.DEFAULT;
    private static final NumberOptions FORMULAS = new NumberOptions(true, false, Locale.ROOT);
    private static final NumberOptions CHARTS = new NumberOptions(false, true, Locale.ROOT);

    private TooltipNodePalette palette;

    @BeforeAll
    public static void setUpLanguage() {
        Map<String, String> translations = new HashMap<>(CoreLang.TRANSLATION_MAP);

        translations.put(COUNT, "Count: %s");
        translations.put(CONDITION, "Killed by player");
        translations.put("enchantment.minecraft.looting", "Looting");
        translations.put("enchantment.minecraft.fortune", "Fortune");
        translations.put("enchantment.level.1", "I");
        translations.put("enchantment.level.2", "II");
        translations.put("enchantment.level.3", "III");
        Language.inject(new MapLanguage(translations));

        NumberFunctions.register(NumberFunction.builder(BOOM).arity(1)
                .bounds((a) -> {
                    throw new IllegalStateException("boom");
                })
                .build());
    }

    @BeforeEach
    public void setUpPalette() {
        palette = new TooltipNodePalette(MOD_ID);
        TooltipContext.setPalette(palette);
    }

    @AfterEach
    public void clearPalette() {
        TooltipContext.clearPalette();
    }

    @Test
    public void testNumberNodeSurvivesRoundTrip() {
        TooltipNumber number = new TooltipNumber(add(uniformInt(0, 2), LOOTING), true, NumberInterval.closed(0, 64));
        TooltipNode node = TooltipBuilder.number(number.expr(), number.percent(), number.limit()).build(COUNT);
        TestServerUtils server = new TestServerUtils(palette, List.of());
        TestClientUtils client = new TestClientUtils(List.of());
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());

        palette.encode(server, buf);
        client.getTooltipCache().decode(client, buf);

        TooltipNode decoded = client.getTooltipCache().getNodeById(palette.getNodeId(node));

        assertEquals(number, decoded.getNumber());
        assertEquals(0, buf.readableBytes());
    }

    @Test
    public void testEqualNumbersShareOneNode() {
        TooltipNode first = TooltipBuilder.number(add(uniformInt(0, 2), LOOTING)).build(COUNT);
        TooltipNode second = TooltipBuilder.number(add(uniformInt(0, 2), LOOTING)).build(COUNT);
        TooltipNode percent = TooltipBuilder.percent(add(uniformInt(0, 2), LOOTING)).build(COUNT);

        assertSame(first, second);
        assertNotSame(first, percent);
    }

    @Test
    public void testLevelRows() {
        TooltipNode node = TooltipBuilder.number(looting()).build(COUNT);

        assertEquals(List.of(
                "Count: 0 to 2",
                "  -> Looting I: 0 to 3  ~1 to 2 (33%)",
                "  -> Looting II: 0 to 4  ~2 (33%)",
                "  -> Looting III: 0 to 5  ~2 to 3 (28%)"
        ), lines(node, TEXT));
    }

    @Test
    public void testLevelFormulaRow() {
        TooltipNode node = TooltipBuilder.number(add(constant(1), binomial(add(constant(3), FORTUNE), constant(0.5714)))).build(COUNT);

        assertEquals(List.of(
                "Count: 1 + binom(3; 57.14%)  ~3 (42%)",
                "  -> Fortune LVL: 1 + binom(3 + LVL; 57.14%)",
                "  -> Fortune I: 1 to 5  ~3 (36%)",
                "  -> Fortune II: 1 to 6  ~4 (34%)",
                "  -> Fortune III: 1 to 7  ~4 (29%)"
        ), lines(node, FORMULAS));
    }

    @Test
    public void testChanceWithLevel() {
        TooltipNode node = TooltipBuilder.percent(add(constant(0.025), mul(LOOTING, constant(0.01)))).build(COUNT);

        assertEquals(List.of(
                "Count: 2.5%",
                "  -> Looting I: 3.5%",
                "  -> Looting II: 4.5%",
                "  -> Looting III: 5.5%"
        ), lines(node, TEXT));
    }

    @Test
    public void testLimitClampsRangeAndMode() {
        TooltipNode node = TooltipBuilder.number(uniformInt(10, 100), false, NumberInterval.closed(0, 64)).build(COUNT);

        assertEquals(List.of("Count: 10 to 64  ~64 (41%)"), lines(node, TEXT));
    }

    @Test
    public void testConditionalBranchesShowTheirConditions() {
        TooltipNode node = TooltipBuilder.number(cond(List.of(new Branch(0, uniformInt(2, 4))), constant(1)))
                .add(TooltipBuilder.keyOnly(CONDITION))
                .build(COUNT);

        assertEquals(List.of(
                "Count: ",
                "  -> 2 to 4",
                "    -> Killed by player",
                "  -> otherwise 1"
        ), lines(node, TEXT));
    }

    @Test
    public void testConditionIndexOutOfRangeIsIgnored() {
        TooltipNode node = TooltipBuilder.number(cond(List.of(new Branch(3, uniformInt(2, 4))), constant(1))).build(COUNT);

        assertEquals(List.of(
                "Count: ",
                "  -> 2 to 4",
                "  -> otherwise 1"
        ), lines(node, TEXT));
    }

    @Test
    public void testWeightedEntriesGetOwnLines() {
        TooltipNode node = TooltipBuilder.number(weighted(List.of(new WeightedEntry(9, constant(1)), new WeightedEntry(1, constant(2))))).build(COUNT);

        assertEquals(List.of(
                "Count: 1 to 2  ~1 (90%)",
                "  -> 1 (90%)",
                "  -> 2 (10%)"
        ), lines(node, TEXT));
    }

    @Test
    public void testChartOverlaysLevels() {
        NumberExpr glowstone = clamp(add(uniformInt(2, 4), uniformInt(constant(0), FORTUNE)), constant(1), constant(4));
        List<TooltipLine> lines = CoreTooltipUtils.toLines(TooltipBuilder.number(glowstone).build(COUNT), 0, true, TooltipStyle.DEFAULT, CHARTS);
        TooltipLine.Chart chart = (TooltipLine.Chart) lines.get(lines.size() - 1);

        assertEquals(5, lines.size());
        assertEquals(4, chart.series().size());
        assertEquals(3, chart.series().get(0).heights().size());
        assertTrue(chart.series().get(0).base());
        chart.series().get(0).heights().forEach((h) -> assertEquals(1.0 / 3 / 0.75, h, 1e-9));
        assertEquals(1.0, chart.series().get(3).heights().get(2), 1e-9);
        assertEquals(List.of(false, false, false), chart.series().get(0).modes());
        assertEquals("2", chart.series().get(0).min().getString());
        assertEquals("4", chart.series().get(0).max().getString());
        assertEquals(chart.series().get(1).color(), labelColor(lines.get(1)));
    }

    @Test
    public void testWideTriangleChartIsSmooth() {
        TooltipLine.Chart chart = chart(add(constant(-80), uniformInt(0, 136), uniformInt(0, 136)));
        List<Double> heights = chart.series().get(0).heights();
        double step = heights.get(1) - heights.get(0);

        assertEquals(55, chart.series().get(0).heights().size());
        assertEquals(1, chart.series().get(0).modes().stream().filter((m) -> m).count());
        assertTrue(chart.series().get(0).modes().get(27));

        for (int i = 1; i <= 26; i++) {
            assertEquals(step, heights.get(i) - heights.get(i - 1), 1e-9);
        }
        for (int i = 29; i < 54; i++) {
            assertEquals(-step, heights.get(i) - heights.get(i - 1), 1e-9);
        }

        assertTrue(heights.get(54) < heights.get(53));
        assertEquals("−80", chart.series().get(0).min().getString());
        assertEquals("192", chart.series().get(0).max().getString());
    }

    @Test
    public void testWideBiasedChartIsMonotone() {
        TooltipLine.Chart chart = chart(add(constant(48), uniformInt(constant(0), add(uniformInt(0, 143), constant(7)))));
        List<Double> heights = chart.series().get(0).heights();

        assertEquals(51, chart.series().get(0).heights().size());
        assertEquals(List.of(true, true, true, false), chart.series().get(0).modes().subList(0, 4));

        for (int i = 1; i < heights.size(); i++) {
            assertTrue(heights.get(i) <= heights.get(i - 1) + 1e-12, "column " + i);
        }
    }

    @Test
    public void testNoChartWithoutMode() {
        List<TooltipLine> lines = CoreTooltipUtils.toLines(TooltipBuilder.number(uniformInt(0, 2)).build(COUNT), 0, true, TooltipStyle.DEFAULT, CHARTS);

        assertEquals(List.of("Count: 0 to 2"), strings(lines));
    }

    @Test
    public void testFailingNumberRendersErrorInsteadOfThrowing() {
        TooltipNode node = TooltipBuilder.number(fn(BOOM, constant(1))).build(COUNT);
        List<TooltipLine> lines = CoreTooltipUtils.toLines(node, 0, true, TooltipStyle.DEFAULT, CHARTS);
        Component value = (Component) ((TranslatableContents) ((TooltipLine.Text) lines.get(0)).component().getSiblings().get(0).getContents()).getArgs()[0];

        assertEquals(List.of("Count: ?"), strings(lines));
        assertEquals(TooltipStyle.DEFAULT.error().getColor(), value.getStyle().getColor());
    }

    @Test
    public void testFailingBoundsGivesUnknownSlot() {
        NumberInterval bounds = NumberFormatter.slotBounds(fn(BOOM, constant(1)));

        assertEquals(NumberInterval.UNKNOWN, bounds);
        assertEquals("?", NumberFormatter.slot(bounds));
    }

    @Test
    public void testFailingConverterGivesOpaque() {
        ManagedRegistry<Class<?>, BiFunction<Object, Object, NumberExpr>> registry = new ManagedRegistry<>(MOD_ID, "test", true, HashMap::new, ManagedRegistry::classKeyName, null);

        registry.put(String.class, (u, t) -> {
            throw new IllegalStateException("broken shim");
        });

        assertEquals(opaque("test:broken"), NumberConverters.convert(MOD_ID, registry, null, "value", (t) -> "test:broken"));
    }

    @Test
    public void testMissingConverterGivesOpaque() {
        ManagedRegistry<Class<?>, BiFunction<Object, Object, NumberExpr>> registry = new ManagedRegistry<>(MOD_ID, "test", true, HashMap::new, ManagedRegistry::classKeyName, null);

        assertEquals(opaque("test:missing"), NumberConverters.convert(MOD_ID, registry, null, 5, (t) -> "test:missing"));
        assertEquals(opaque(Integer.class.getTypeName()), NumberConverters.convert(MOD_ID, registry, null, 5, (t) -> {
            throw new IllegalStateException("no type");
        }));
    }

    @NotNull
    private static NumberExpr looting() {
        return add(uniformInt(0, 2), fn(NumberFunctions.ROUND, mul(LOOTING, uniformFloat(0, 1))));
    }

    private static int labelColor(TooltipLine line) {
        Component row = ((TooltipLine.Text) line).component();
        Component translated = row.getSiblings().get(1);
        Object[] args = ((TranslatableContents) translated.getContents()).getArgs();

        return ((Component) args[0]).getStyle().getColor().getValue() | 0xFF000000;
    }

    @NotNull
    private static List<String> lines(TooltipNode node, NumberOptions options) {
        return strings(CoreTooltipUtils.toLines(node, 0, true, TooltipStyle.DEFAULT, options));
    }

    @NotNull
    private static TooltipLine.Chart chart(NumberExpr expr) {
        List<TooltipLine> lines = CoreTooltipUtils.toLines(TooltipBuilder.number(expr).build(COUNT), 0, true, TooltipStyle.DEFAULT, CHARTS);

        return (TooltipLine.Chart) lines.get(lines.size() - 1);
    }

    @NotNull
    private static List<String> strings(List<TooltipLine> lines) {
        return TestUtils.toComponents(lines).stream().map(TestUtils::componentToPlainString).toList();
    }

    private static final class MapLanguage extends Language {
        private final Map<String, String> translations;

        private MapLanguage(Map<String, String> translations) {
            this.translations = translations;
        }

        @NotNull
        @Override
        public String getOrDefault(String key, String fallback) {
            return translations.getOrDefault(key, fallback);
        }

        @Override
        public boolean has(String key) {
            return translations.containsKey(key);
        }

        @Override
        public boolean isDefaultRightToLeft() {
            return false;
        }

        @NotNull
        @Override
        public FormattedCharSequence getVisualOrder(FormattedText text) {
            return (sink) -> text.visit((style, s) -> StringDecomposer.iterateFormatted(s, style, sink) ? Optional.empty() : FormattedText.STOP_ITERATION, Style.EMPTY).isPresent();
        }
    }
}

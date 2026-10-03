package com.yanny.aci.test;

import com.yanny.aci.api.*;
import com.yanny.aci.language.CoreLang;
import com.yanny.aci.number.NumberFormatter;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

import java.util.*;

import static com.yanny.aci.api.NumberExpr.*;
import static com.yanny.aci.api.NumberFunctions.*;
import static org.junit.jupiter.api.Assertions.*;

public class NumberExprTest {
    private static final Var LOOTING = level("minecraft:looting", 3);
    private static final Var FORTUNE = level("minecraft:fortune", 3);

    private static final Identifier BIASED_HEIGHT = test("biased_height");
    private static final Identifier VERY_BIASED_HEIGHT = test("very_biased_height");
    private static final Identifier VERY_BIASED_INT = test("very_biased_int");
    private static final Identifier TRAPEZOID_INT = test("trapezoid_int");
    private static final Identifier DICE = test("dice");
    private static final Identifier LOG = test("log");
    private static final Map<String, String> TEST_TRANSLATIONS = new HashMap<>();

    static {
        TEST_TRANSLATIONS.put("test.number.fn.biased_height", "biased");
        TEST_TRANSLATIONS.put("test.number.fn.very_biased_height", "very_biased");
        TEST_TRANSLATIONS.put("test.number.fn.very_biased_int", "very_biased");
        TEST_TRANSLATIONS.put("test.number.fn.trapezoid_int", "trapezoid");
        TEST_TRANSLATIONS.put("test.number.fn.dice", "dice");
        TEST_TRANSLATIONS.put("test.number.fn.log", "log");

        NumberFunctions.register(NumberFunction.builder(BIASED_HEIGHT).arity(3)
                .expand((a) -> add(a.get(0), uniformInt(constant(0), add(uniformInt(constant(0), sub(sub(a.get(1), a.get(0)), a.get(2))), a.get(2), constant(-1)))))
                .format((a, f) -> a.get(2).equals(constant(1)) ? call(BIASED_HEIGHT, f.subList(0, 2)) : null)
                .build());
        NumberFunctions.register(NumberFunction.builder(VERY_BIASED_HEIGHT).arity(3)
                .expand((a) -> {
                    NumberExpr k = uniformInt(add(a.get(0), a.get(2)), a.get(1));
                    NumberExpr l = uniformInt(a.get(0), sub(k, constant(1)));

                    return uniformInt(a.get(0), add(l, constant(-1), a.get(2)));
                })
                .build());
        NumberFunctions.register(NumberFunction.builder(VERY_BIASED_INT).arity(2)
                .expand((a) -> add(a.get(0), uniformInt(constant(0), uniformInt(constant(0), uniformInt(constant(0), sub(a.get(1), a.get(0)))))))
                .build());
        NumberFunctions.register(NumberFunction.builder(TRAPEZOID_INT).arity(3)
                .expand(NumberExprTest::trapezoidInt)
                .build());
        NumberFunctions.register(NumberFunction.builder(DICE).arity(2).units(NumberFunction.Unit.PLAIN)
                .bounds((a) -> NumberInterval.closed(a.get(0).lo(), a.get(0).hi() * a.get(1).hi()))
                .distributionOfParameters((p) -> {
                    NumberDistribution sum = NumberDistribution.constant(0);
                    NumberDistribution die = NumberDistribution.uniformInt(1, (long) p[1]);

                    for (int i = 0; i < p[0] && sum != null && die != null; i++) {
                        sum = sum.plus(die);
                    }

                    return sum;
                })
                .build());
        NumberFunctions.register(NumberFunction.builder(LOG).arity(1)
                .evaluate((v) -> Math.log(v[0]))
                .bounds((a) -> a.get(0).monotone(Math::log))
                .distribution((a) -> a.get(0).map(Math::log))
                .build());
    }

    @Test
    public void testShiftConditions() {
        NumberExpr inner = cond(List.of(new Branch(0, constant(2))), constant(1));
        NumberExpr expr = add(cond(List.of(new Branch(1, inner), new Branch(-1, constant(5))), constant(0)), constant(3));
        NumberExpr shiftedInner = cond(List.of(new Branch(2, constant(2))), constant(1));

        assertEquals(add(cond(List.of(new Branch(3, shiftedInner), new Branch(-1, constant(5))), constant(0)), constant(3)), expr.shiftConditions(2));
        assertSame(expr, expr.shiftConditions(0));
    }

    @Test
    public void testConstant() {
        assertLevels(constant(1), false, List.of("1"));
    }

    @Test
    public void testUniform() {
        assertLevels(uniformInt(0, 2), false, List.of("0 to 2"));
        assertEquals("0 to 2", format(uniformInt(0, 2), true, false));
    }

    @Test
    public void testLooting() {
        NumberExpr count = add(uniformInt(0, 2), fn(ROUND, mul(LOOTING, uniformFloat(0, 1))));

        assertLevels(count, false, List.of(
                "0 to 2",
                "0 to 3  ~1 to 2 (33%)",
                "0 to 4  ~2 (33%)",
                "0 to 5  ~2 to 3 (28%)"
        ));
        assertEquals("U[0; 2] + round(LVL × U[0; 1))", format(count, true, false));
    }

    @Test
    public void testBinomial() {
        NumberExpr count = add(constant(1), binomial(add(constant(3), FORTUNE), constant(0.5714286F)));

        assertLevels(count, false, List.of(
                "1 to 4  ~3 (42%)",
                "1 to 5  ~3 (36%)",
                "1 to 6  ~4 (34%)",
                "1 to 7  ~4 to 5 (29%)"
        ));
        assertEquals("1 + binom(3; 57.14%)  ~3 (42%)", format(bindLevels(count, 0), true, false));
        assertEquals("1 + binom(3 + LVL; 57.14%)", format(count, true, false));
    }

    @Test
    public void testOreDrops() {
        NumberExpr count = mul(constant(1), max(constant(1), uniformInt(constant(0), add(FORTUNE, constant(1)))));

        assertLevels(count, false, List.of(
                "1",
                "1 to 2  ~1 (67%)",
                "1 to 3  ~1 (50%)",
                "1 to 4  ~1 (40%)"
        ));
        assertEquals("max(1; U[0; LVL + 1])", format(count, true, false));
        assertEquals("1 to 11  ~1 (17%)", format(count.bind(FORTUNE, 10), false, false));
    }

    @Test
    public void testClampOverSum() {
        NumberExpr count = clamp(add(uniformInt(2, 4), uniformInt(constant(0), mul(FORTUNE, constant(1)))), constant(1), constant(4));

        assertLevels(count, false, List.of(
                "2 to 4",
                "2 to 4  ~4 (50%)",
                "2 to 4  ~4 (67%)",
                "2 to 4  ~4 (75%)"
        ));
        assertEquals("clamp(U[2; 4] + U[0; LVL]; 1; 4)", format(count, true, false));
    }

    @Test
    public void testChanceWithLevel() {
        NumberExpr chance = add(constant(0.025), mul(LOOTING, constant(0.01)));

        assertLevels(chance, true, List.of("2.5%", "3.5%", "4.5%", "5.5%"));
        assertEquals("2.5% + LVL × 1%", format(chance, true, true));
        assertEquals("12.5%", format(chance.bind(LOOTING, 10), false, true));
    }

    @Test
    public void testLookup() {
        NumberExpr chance = lookup(FORTUNE, List.of(constant(0.05), constant(0.0625), constant(0.083333336), constant(0.1)), null);

        assertLevels(chance, true, List.of("5%", "6.25%", "8.33%", "10%"));
        assertEquals("[5%; 6.25%; 8.33%; 10%][min(LVL; 3)]", format(chance, true, true));
        assertEquals("10%", format(chance.bind(FORTUNE, 7), false, true));
    }

    @Test
    public void testHalfOpenInterval() {
        NumberExpr damage = uniformFloat(0.15, 0.8);

        assertEquals("15% to 80%", format(damage, false, true));
        assertEquals("U[15%; 80%)", format(damage, true, true));
        assertEquals(new NumberInterval(0.15, 0.8, true, false), damage.bounds());
    }

    @Test
    public void testUnboundedRange() {
        assertEquals("≥ 2", format(atLeast(2), false, false));
        assertEquals("≥ 2", format(atLeast(2), true, false));
        assertEquals("≤ 5", format(atMost(5), true, false));
    }

    @Test
    public void testScore() {
        NumberExpr score = score(NumberText.key(CoreLang.Numbers.TARGET_THIS.singular()), "kills");
        NumberInterval stack = NumberInterval.closed(0, 64);

        assertEquals("0 to 64 (score \"kills\" (this))", render(NumberFormatter.format(score, false, false, stack)));
        assertEquals("0 to 16 (score \"kills\" (this))", render(NumberFormatter.format(score, false, false, NumberInterval.closed(0, 16))));
        assertEquals("score(this; kills)", render(NumberFormatter.format(score, true, false, stack)));
        assertEquals("any (score \"kills\" (this))", format(score, false, false));
        assertEquals("1 to 129 (score \"kills\" (this))", render(NumberFormatter.format(add(mul(score, constant(2)), constant(1)), false, false, NumberInterval.closed(1, 129))));
        assertEquals("score(this; kills) × 2 + 1", format(add(mul(score, constant(2)), constant(1)), true, false));
    }

    @Test
    public void testSetCountAboveMaxStack() {
        assertEquals("64", render(NumberFormatter.format(constant(100), false, false, NumberInterval.closed(0, 64))));
    }

    @Test
    public void testOpaque() {
        NumberExpr unknown = opaque("examplemod:custom_count");

        assertEquals("? (examplemod:custom_count)", format(unknown, false, false));
        assertEquals("? (examplemod:custom_count)", format(unknown, true, false));
        assertTrue(add(unknown, constant(1)).bounds().isUnknown());
        assertTrue(unknown.mode().isEmpty());
    }

    @Test
    public void testConditional() {
        NumberExpr count = cond(List.of(new Branch(0, uniformInt(2, 4))), constant(1));

        assertEquals(NumberInterval.closed(1, 4), count.bounds());
        assertTrue(count.mode().isEmpty());
        assertEquals("U[2; 4] | 1", format(add(count, constant(0)), true, false));
    }

    @Test
    public void testNestedClampedInt() {
        NumberExpr count = clamp(uniformInt(-10, 4), constant(0), constant(4));

        assertEquals("0 to 4  ~0 (73%)", format(count, false, false));
        assertEquals("clamp(U[−10; 4]; 0; 4)  ~0 (73%)", format(count, true, false));
    }

    @Test
    public void testClampedNormalInt() {
        NumberExpr count = fn(TRUNC, clamp(fn(NORMAL, constant(3), constant(1)), constant(1), constant(5)));

        assertEquals("1 to 5  ~2 to 3 (34%)", format(count, false, false));
        assertEquals("trunc(clamp(normal(3; 1); 1; 5))  ~2 to 3 (34%)", format(count, true, false));
    }

    @Test
    public void testClampedNormalFloat() {
        NumberExpr value = clamp(fn(NORMAL, constant(4.5), constant(1)), constant(1), constant(8));

        assertEquals("1 to 8  ~4.5", format(value, false, false));
    }

    @Test
    public void testTrapezoidFloatPlateau() {
        NumberExpr value = fn(TRAPEZOID_FLOAT, constant(0), constant(40), constant(20));

        assertEquals("0 to 40  ~10 to 30", format(value, false, false));
        assertEquals("trapezoid(0; 40; 20)  ~10 to 30", format(value, true, false));
    }

    @Test
    public void testWeightedList() {
        NumberExpr count = weighted(List.of(new WeightedEntry(9, constant(1)), new WeightedEntry(1, constant(2))));

        assertEquals("1 to 2  ~1 (90%)", format(count, false, false));
        assertEquals("1 (90%) | 2 (10%)  ~1 (90%)", format(count, true, false));
    }

    @Test
    public void testBiasedToBottom() {
        NumberExpr value = fn(BIASED_TO_BOTTOM, constant(-64), constant(16));

        assertEquals("−64 to 16  ~−64 (6%)", format(value, false, false));
        assertEquals("biased(−64; 16)  ~−64 (6%)", format(value, true, false));
        assertMatchesSampling(value, (r) -> -64 + r.nextInt(r.nextInt(16 + 64 + 1) + 1));
    }

    @Test
    public void testBiasedToBottomHeightExtension() {
        NumberExpr height = fn(BIASED_HEIGHT, constant(-64), constant(16), constant(1));

        assertEquals("−64 to 15  ~−64 (6%)", format(height, false, false));
        assertEquals("biased(−64; 16)  ~−64 (6%)", format(height, true, false));
        assertEquals("biased(0; 10; 3)  ~0 to 2 (18%)", format(fn(BIASED_HEIGHT, constant(0), constant(10), constant(3)), true, false));
        assertMatchesSampling(fn(BIASED_HEIGHT, constant(3), constant(9), constant(2)), (r) -> 3 + r.nextInt(r.nextInt(9 - 3 - 2 + 1) + 2));
    }

    @Test
    public void testVeryBiasedHeightExtension() {
        assertMatchesSampling(fn(VERY_BIASED_HEIGHT, constant(0), constant(8), constant(2)), (r) -> {
            int k = nextInt(r, 2, 8);
            int l = nextInt(r, 0, k - 1);
            return nextInt(r, 0, l - 1 + 2);
        });
    }

    @Test
    public void testVeryBiasedIntExtension() {
        assertMatchesSampling(fn(VERY_BIASED_INT, constant(-2), constant(6)), (r) -> -2 + r.nextInt(r.nextInt(r.nextInt(6 + 2 + 1) + 1) + 1));
        assertEquals(NumberInterval.closed(-2, 6), fn(VERY_BIASED_INT, constant(-2), constant(6)).bounds());
    }

    @Test
    public void testTrapezoidIntExtension() {
        assertMatchesSampling(fn(TRAPEZOID_INT, constant(-3), constant(7), constant(2)), (r) -> {
            int l = (10 - 2) / 2;
            return -3 + nextInt(r, 0, 10 - l) + nextInt(r, 0, l);
        });
        assertMatchesSampling(fn(TRAPEZOID_INT, constant(-4), constant(4), constant(0)), (r) -> r.nextInt(5) - r.nextInt(5));
        assertEquals("trapezoid(−3; 7; 2)  ~1 to 3 (14%)", format(fn(TRAPEZOID_INT, constant(-3), constant(7), constant(2)), true, false));
    }

    @Test
    public void testPrimitiveExtension() {
        NumberExpr dice = fn(DICE, constant(2), constant(6));

        assertEquals("2 to 12  ~7 (17%)", format(dice, false, false));
        assertEquals("dice(2; 6)  ~7 (17%)", format(dice, true, false));
        assertEquals("dice(LVL; 6)", format(fn(DICE, LOOTING, constant(6)), true, false));
        assertEquals("1 to 18  ~6 (12%)", format(fn(DICE, uniformInt(1, 3), constant(6)), false, false));
        assertEquals(constant(0), fn(LOG, constant(1)));
        assertEquals(NumberInterval.closed(0, Math.log(4)), fn(LOG, uniformInt(1, 4)).bounds());
    }

    @Test
    public void testUnregisteredFunction() {
        NumberExpr unknown = new Fn(test("not_registered"), List.of(constant(1), uniformInt(1, 2)));

        assertEquals(unknown, fn(test("not_registered"), constant(1), uniformInt(1, 2)));
        assertTrue(unknown.bounds().isUnknown());
        assertNull(unknown.distribution());
        assertEquals("? (test:not_registered)", format(unknown, false, false));
        assertEquals("? (test:not_registered) + 1", format(add(unknown, constant(1)), true, false));
        assertEquals(unknown, roundTrip(unknown));
    }

    @Test
    public void testRegistration() {
        NumberFunction duplicate = NumberFunction.builder(ADD).build();
        NumberFunction add = NumberFunctions.get(ADD);

        assertNotNull(add);
        assertDoesNotThrow(() -> NumberFunctions.register(duplicate));
        assertSame(add, NumberFunctions.get(ADD));
        assertDoesNotThrow(() -> NumberFunctions.register(add));
        assertThrows(IllegalArgumentException.class, () -> fn(CLAMP, constant(1)));
    }

    @Test
    public void testTruncOfHalfOpenInterval() {
        NumberExpr value = fn(TRUNC, uniformFloat(0, 3));

        assertEquals(NumberInterval.closed(0, 2), value.bounds());
        assertEquals(NumberInterval.closed(-3, 0), fn(TRUNC, uniformFloat(-3, 0)).bounds());
        assertEquals(NumberInterval.closed(-2, 0), fn(TRUNC, range(constant(-3), constant(0), false, false)).bounds());
        assertEquals(NumberInterval.closed(0, 3), fn(ROUND, uniformFloat(0, 3)).bounds());
        assertTrue(value.mode().isEmpty());
    }

    @Test
    public void testIntervalArithmetic() {
        assertEquals(NumberInterval.closed(55, 64), min(add(constant(50), uniformInt(5, 19)), constant(64)).bounds());
        assertEquals(NumberInterval.closed(-8, 12), mul(uniformInt(-2, 3), constant(4)).bounds());
        assertEquals(NumberInterval.closed(0, 9), fn(POW, uniformInt(-3, 2), constant(2)).bounds());
        assertEquals(new NumberInterval(0, 5, true, false), fn(FLOOR_MOD, uniformInt(-100, 100), constant(5)).bounds());
        assertEquals(NumberInterval.closed(0, 3), fn(ABS, uniformInt(-3, 2)).bounds());
        assertEquals(NumberInterval.ALL, div(constant(1), uniformInt(-1, 1)).bounds());
    }

    @Test
    public void testSimplification() {
        assertEquals(constant(5), add(constant(2), constant(3)));
        assertEquals(uniformInt(0, 2), add(uniformInt(0, 2), constant(0)));
        assertEquals(constant(0), mul(LOOTING, constant(0)));
        assertEquals(constant(3), uniformInt(3, 3));
        assertEquals(uniformInt(0, 2), bindLevels(add(uniformInt(0, 2), fn(ROUND, mul(LOOTING, uniformFloat(0, 1)))), 0));
        assertEquals("LVL − 1", format(add(LOOTING, constant(-1)), true, false));
        assertEquals("(LVL + 1) × 2", format(mul(add(LOOTING, constant(1)), constant(2)), true, false));
        assertEquals("LVL − (LVL − 1)", format(sub(LOOTING, sub(LOOTING, constant(1))), true, false));
        assertEquals("−(LVL + 1)", format(fn(NEG, add(LOOTING, constant(1))), true, false));
    }

    @Test
    public void testIsSimple() {
        assertTrue(constant(1).isSimple());
        assertTrue(range(1, 5).isSimple());
        assertTrue(atLeast(2).isSimple());
        assertTrue(uniformInt(1, 5).isSimple());
        assertFalse(uniformFloat(1, 5).isSimple());
        assertFalse(range(constant(1), constant(5), true, false).isSimple());
        assertFalse(binomial(constant(3), constant(0.5)).isSimple());
        assertFalse(LOOTING.isSimple());
    }

    @Test
    public void testVars() {
        NumberExpr expr = add(mul(LOOTING, constant(2)), FORTUNE, LOOTING);

        assertEquals(List.of(LOOTING, FORTUNE), new ArrayList<>(expr.vars()));
        assertEquals(NumberInterval.closed(0, 12), expr.bounds());
    }

    @Test
    public void testRoundTrip() {
        List<NumberExpr> expressions = List.of(
                constant(-2.5),
                range(1, 5),
                atLeast(2),
                range(uniformInt(1, 2), null, false, false),
                add(constant(1), binomial(add(constant(3), FORTUNE), constant(0.5714286F))),
                lookup(FORTUNE, List.of(constant(0.05), constant(0.1)), constant(1)),
                cond(List.of(new Branch(3, uniformInt(2, 4)), new Branch(1, constant(7))), constant(1)),
                weighted(List.of(new WeightedEntry(9, constant(1)), new WeightedEntry(1, opaque("a:b")))),
                fn(TRUNC, clamp(fn(NORMAL, constant(3), constant(1)), constant(1), constant(5))),
                score(NumberText.key(CoreLang.Numbers.TARGET_KILLER.singular()), "deaths"),
                fn(VERY_BIASED_HEIGHT, constant(0), constant(10), constant(2))
        );

        for (NumberExpr expr : expressions) {
            assertEquals(expr, roundTrip(expr));
        }
    }

    @Test
    public void testLocaleFormatting() {
        assertEquals("0,15", NumberText.formatNumber(0.15, Locale.forLanguageTag("sk")));
        assertEquals("0.15", NumberText.formatNumber(0.15, Locale.ROOT));
        assertEquals("−64", NumberText.formatNumber(-64, Locale.ROOT));
        assertEquals("8.33", NumberText.formatNumber(8.333333, Locale.ROOT));
        assertEquals("0", NumberText.formatNumber(-0.001, Locale.ROOT));
    }

    @Test
    public void testModeRecord() {
        NumberMode mode = uniformFloat(0, 1).mode().orElse(null);

        assertNull(mode);
        assertFalse(new NumberMode(1, 2, Double.NaN).hasProbability());
    }

    private static NumberExpr trapezoidInt(List<NumberExpr> a) {
        if (!(a.get(0) instanceof Const min) || !(a.get(1) instanceof Const max) || !(a.get(2) instanceof Const plateau)) {
            return null;
        }
        if (plateau.value() == 0 && max.value() == -min.value()) {
            return sub(uniformInt(0, max.value()), uniformInt(0, max.value()));
        }

        double k = max.value() - min.value();

        if (plateau.value() >= k) {
            return uniformInt(min.value(), max.value());
        }

        double l = Math.floor((k - plateau.value()) / 2);

        return add(min, uniformInt(0, k - l), uniformInt(0, l));
    }

    private static NumberText call(Identifier id, List<NumberText> args) {
        List<NumberText> parts = new ArrayList<>();

        parts.add(NumberText.key(NumberFunction.translationKey(id)));
        parts.add(NumberText.str("("));

        for (int i = 0; i < args.size(); i++) {
            if (i > 0) {
                parts.add(NumberText.str("; "));
            }

            parts.add(args.get(i));
        }

        parts.add(NumberText.str(")"));
        return NumberText.seq(parts);
    }

    private static Identifier test(String path) {
        return Identifier.fromNamespaceAndPath("test", path);
    }

    private static NumberExpr roundTrip(NumberExpr expr) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());

        expr.encode(buf);

        NumberExpr decoded = NumberExpr.decode(buf);

        assertEquals(0, buf.readableBytes());
        return decoded;
    }

    private static int nextInt(Random random, int min, int max) {
        return min >= max ? min : random.nextInt(max - min + 1) + min;
    }

    private interface Sampler {
        int sample(Random random);
    }

    private static void assertMatchesSampling(NumberExpr expr, Sampler sampler) {
        NumberDistribution distribution = expr.distribution();
        Random random = new Random(1);
        TreeMap<Double, Integer> counts = new TreeMap<>();
        int samples = 400_000;

        assertNotNull(distribution);
        assertEquals(1, distribution.atoms().values().stream().mapToDouble(Double::doubleValue).sum(), 1e-9);

        for (int i = 0; i < samples; i++) {
            counts.merge((double) sampler.sample(random), 1, Integer::sum);
        }

        assertEquals(counts.keySet(), distribution.atoms().keySet());
        counts.forEach((v, c) -> assertEquals(distribution.atoms().get(v), c / (double) samples, 0.005, "value " + v));
    }

    private static void assertLevels(NumberExpr expr, boolean percent, List<String> expected) {
        Var variable = expr.vars().stream().filter((v) -> v.type().equals(LEVEL)).findFirst().orElse(null);
        List<String> lines = new ArrayList<>();

        lines.add(format(bindLevels(expr, 0), false, percent));

        if (variable != null) {
            for (int level = 1; level <= variable.max(); level++) {
                lines.add(format(expr.bind(variable, level), false, percent));
            }
        }

        assertEquals(expected, lines);
    }

    private static NumberExpr bindLevels(NumberExpr expr, double value) {
        for (Var variable : expr.vars()) {
            if (variable.type().equals(LEVEL)) {
                expr = expr.bind(variable, value);
            }
        }

        return expr;
    }

    private static String format(NumberExpr expr, boolean formula, boolean percent) {
        return render(NumberFormatter.format(expr, formula, percent));
    }

    private static String render(NumberFormatter.Formatted formatted) {
        return formatted.mode() != null ? render(formatted.value()) + "  " + render(formatted.mode()) : render(formatted.value());
    }

    private static String render(NumberText text) {
        if (text instanceof NumberText.Key key) {
            String template = CoreLang.TRANSLATION_MAP.getOrDefault(key.key(), TEST_TRANSLATIONS.get(key.key()));

            assertNotNull(template, "missing translation " + key.key());
            return String.format(Locale.ROOT, template, key.args().stream().map(NumberExprTest::render).toArray());
        } else if (text instanceof NumberText.Num num) {
            return NumberText.formatNumber(num.value(), Locale.ROOT);
        } else if (text instanceof NumberText.Str str) {
            return str.text();
        } else if (text instanceof NumberText.Seq seq) {
            StringBuilder builder = new StringBuilder();

            seq.parts().forEach((p) -> builder.append(render(p)));
            return builder.toString();
        }

        throw new IllegalStateException();
    }
}

package com.yanny.ali.plugin.glm;

import com.yanny.ali.api.IServerUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

final class LootContextProbe {
    private static final Trap TRAP = new Trap();

    @NotNull
    static Verdict probe(IServerUtils utils, LootItemCondition condition, LootPage page, List<ILootContextPreparer> preparers, Function<ResourceLocation, LootItemCondition> predicates) {
        try {
            //noinspection DataFlowIssue
            LootContext context = new LootContext(TrapRandom.INSTANCE, 0F, null, (id) -> LootTable.EMPTY, predicates, Map.of(), Map.of());

            context.params = new ParamMap(page);

            for (ILootContextPreparer preparer : preparers) {
                preparer.prepare(utils, context, page);
            }

            return condition.test(context) ? Verdict.yes(true) : Verdict.NO;
        } catch (Throwable e) {
            return Verdict.UNKNOWN;
        }
    }

    private LootContextProbe() {}

    private static final class Trap extends Error {
        private Trap() {
            super(null, null, false, false);
        }
    }

    private static final class ParamMap extends AbstractMap<LootContextParam<?>, Object> {
        private final LootPage page;

        private ParamMap(LootPage page) {
            this.page = page;
        }

        @Override
        public boolean containsKey(Object key) {
            return switch (GlobalLootModifierUtils.getParamState(page, (LootContextParam<?>) key)) {
                case DISALLOWED -> false;
                case REQUIRED -> true;
                case OPTIONAL, GENERIC -> throw TRAP;
            };
        }

        @Override
        public Object get(Object key) {
            if (GlobalLootModifierUtils.getParamState(page, (LootContextParam<?>) key) == ParamState.DISALLOWED) {
                return null;
            }

            throw TRAP;
        }

        @NotNull
        @Override
        public Set<Entry<LootContextParam<?>, Object>> entrySet() {
            throw TRAP;
        }
    }

    private static final class TrapRandom implements RandomSource {
        private static final TrapRandom INSTANCE = new TrapRandom();

        @NotNull
        @Override
        public RandomSource fork() {
            throw TRAP;
        }

        @NotNull
        @Override
        public PositionalRandomFactory forkPositional() {
            throw TRAP;
        }

        @Override
        public void setSeed(long seed) {
            throw TRAP;
        }

        @Override
        public int nextInt() {
            throw TRAP;
        }

        @Override
        public int nextInt(int bound) {
            throw TRAP;
        }

        @Override
        public long nextLong() {
            throw TRAP;
        }

        @Override
        public boolean nextBoolean() {
            throw TRAP;
        }

        @Override
        public float nextFloat() {
            throw TRAP;
        }

        @Override
        public double nextDouble() {
            throw TRAP;
        }

        @Override
        public double nextGaussian() {
            throw TRAP;
        }
    }
}

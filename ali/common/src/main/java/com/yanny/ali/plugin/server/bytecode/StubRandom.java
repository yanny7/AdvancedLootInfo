package com.yanny.ali.plugin.server.bytecode;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

final class StubRandom implements RandomSource {
    private static final int MAX_DRAWS = 1_000;

    private final int pick;
    private int draws;

    StubRandom(int pick) {
        this.pick = pick;
    }

    private static void draw(int draws) {
        if (draws > MAX_DRAWS) {
            throw new Exhausted();
        }
    }

    static int bound(int pick, int bound) {
        return Math.min(pick, bound - 1);
    }

    static float fraction(int pick) {
        return pick == 0 ? 0 : 0.5F;
    }

    @NotNull
    @Override
    public RandomSource fork() {
        return this;
    }

    @NotNull
    @Override
    public PositionalRandomFactory forkPositional() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setSeed(long seed) {
    }

    @Override
    public void consumeCount(int count) {
    }

    @Override
    public int nextInt() {
        draw(++draws);
        return pick;
    }

    @Override
    public int nextInt(int bound) {
        draw(++draws);
        return bound(pick, bound);
    }

    @Override
    public long nextLong() {
        draw(++draws);
        return pick;
    }

    @Override
    public boolean nextBoolean() {
        draw(++draws);
        return pick != 0;
    }

    @Override
    public float nextFloat() {
        draw(++draws);
        return fraction(pick);
    }

    @Override
    public double nextDouble() {
        draw(++draws);
        return fraction(pick);
    }

    @Override
    public double nextGaussian() {
        draw(++draws);
        return 0;
    }

    static final class Legacy extends Random {
        private final int pick;
        private int draws;

        Legacy(int pick) {
            this.pick = pick;
        }

        @Override
        protected int next(int bits) {
            draw(++draws);
            return 0;
        }

        @Override
        public int nextInt() {
            draw(++draws);
            return pick;
        }

        @Override
        public int nextInt(int bound) {
            draw(++draws);
            return bound(pick, bound);
        }

        @Override
        public int nextInt(int origin, int bound) {
            draw(++draws);
            return origin + bound(pick, bound - origin);
        }

        @Override
        public long nextLong() {
            draw(++draws);
            return pick;
        }

        @Override
        public boolean nextBoolean() {
            draw(++draws);
            return pick != 0;
        }

        @Override
        public float nextFloat() {
            draw(++draws);
            return fraction(pick);
        }

        @Override
        public float nextFloat(float bound) {
            draw(++draws);
            return fraction(pick) * bound;
        }

        @Override
        public float nextFloat(float origin, float bound) {
            draw(++draws);
            return origin + fraction(pick) * (bound - origin);
        }

        @Override
        public double nextDouble() {
            draw(++draws);
            return fraction(pick);
        }

        @Override
        public double nextDouble(double bound) {
            draw(++draws);
            return fraction(pick) * bound;
        }

        @Override
        public double nextDouble(double origin, double bound) {
            draw(++draws);
            return origin + fraction(pick) * (bound - origin);
        }

        @Override
        public double nextGaussian() {
            draw(++draws);
            return 0;
        }
    }

    private static final class Exhausted extends Error {
        private Exhausted() {
            super(null, null, false, true);
        }
    }
}

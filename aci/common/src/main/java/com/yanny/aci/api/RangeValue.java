package com.yanny.aci.api;

import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

public final class RangeValue {
    private final float min;
    private final float max;
    private final boolean isRange;
    private final boolean hasScore;
    private final boolean isUnknown;

    public RangeValue(RangeValue rangeValue) {
        this.min = rangeValue.min;
        this.max = rangeValue.max;
        this.isRange = rangeValue.isRange;
        this.hasScore = rangeValue.hasScore;
        this.isUnknown = rangeValue.isUnknown;
    }

    public RangeValue(float value) {
        this(value, value, false, false);
    }

    public RangeValue(boolean hasScore, boolean isUnknown) {
        this(1.0f, 1.0f, hasScore, isUnknown);
    }

    public RangeValue(float min, float max) {
        this(min, max, false, false);
    }

    private RangeValue(float min, float max, boolean hasScore, boolean isUnknown) {
        this.hasScore = hasScore;
        this.isUnknown = isUnknown;

        float trueMin = Math.min(min, max);
        float trueMax = Math.max(min, max);

        if (trueMax - trueMin < 0.00001f) {
            this.min = this.max = trueMin;
            this.isRange = false;
        } else {
            this.min = trueMin;
            this.max = trueMax;
            this.isRange = true;
        }
    }

    public RangeValue(FriendlyByteBuf buf) {
        this.min = buf.readFloat();
        this.max = buf.readFloat();
        this.isRange = buf.readBoolean();
        this.hasScore = buf.readBoolean();
        this.isUnknown = buf.readBoolean();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeFloat(min);
        buf.writeFloat(max);
        buf.writeBoolean(isRange);
        buf.writeBoolean(hasScore);
        buf.writeBoolean(isUnknown);
    }

    @NotNull
    public RangeValue multiply(float value) {
        return new RangeValue(this.min * value, this.max * value, this.hasScore, this.isUnknown);
    }

    @NotNull
    public RangeValue multiply(RangeValue value) {
        float a = this.min * value.min;
        float b = this.min * value.max;
        float c = this.max * value.min;
        float d = this.max * value.max;

        float newMin = Math.min(Math.min(a, b), Math.min(c, d));
        float newMax = Math.max(Math.max(a, b), Math.max(c, d));

        return new RangeValue(newMin, newMax, this.hasScore || value.hasScore, this.isUnknown || value.isUnknown);
    }

    @NotNull
    public RangeValue multiplyMax(float value) {
        return new RangeValue(this.min, this.max * value, this.hasScore, this.isUnknown);
    }

    @NotNull
    public RangeValue addMax(float value) {
        return new RangeValue(this.min, this.max + value, this.hasScore, this.isUnknown);
    }

    @NotNull
    public RangeValue add(float value) {
        return new RangeValue(this.min + value, this.max + value, this.hasScore, this.isUnknown);
    }

    @NotNull
    public RangeValue add(RangeValue value) {
        return new RangeValue(
                this.min + value.min,
                this.max + value.max,
                this.hasScore || value.hasScore,
                this.isUnknown || value.isUnknown
        );
    }

    @NotNull
    public RangeValue clamp(RangeValue minRange, RangeValue maxRange) {
        if (this.isUnknown) {
            return this;
        }

        float newMin = this.min;
        float newMax = this.max;

        if (!minRange.isUnknown) {
            newMin = Math.max(newMin, minRange.min);
            newMax = Math.max(newMax, minRange.min);
        }

        if (!maxRange.isUnknown) {
            newMax = Math.min(newMax, maxRange.max);
            newMin = Math.min(newMin, maxRange.max);
        }

        return new RangeValue(newMin, newMax, this.hasScore, false);
    }

    @NotNull
    public RangeValue clamp(float min, float max) {
        if (this.isUnknown) {
            return this;
        }
        if (this.min < min) {
            min = this.min;
        }
        if (this.max > max) {
            max = this.max;
        }

        return new RangeValue(min, max, this.hasScore, false);
    }

    public float min() {
        return this.min;
    }

    public float max() {
        return this.max;
    }

    public boolean isRange() {
        return isRange;
    }

    public boolean isUnknown() {
        return isUnknown;
    }

    @NotNull
    public String toIntString() {
        String base = isRange ? Math.round(min) + "-" + Math.round(max) : String.valueOf(Math.round(min));
        return appendFlags(base);
    }

    @NotNull
    public String toFloatString() {
        String base = isRange ? format(min) + "-" + format(max) : format(min);
        return appendFlags(base);
    }

    private String appendFlags(String base) {
        if (hasScore) base += "[+Score]";
        if (isUnknown) base += "[+???]";
        return base;
    }

    @NotNull
    @Override
    public String toString() {
        return toFloatString();
    }

    @NotNull
    private static String format(float value) {
        if ((Math.round(value * 100) / 100f) == (int) value) {
            return String.format("%d", (int) value);
        }

        return String.format("%.2f", value);
    }

    @NotNull
    public static String rangeToString(RangeValue min, RangeValue max) {
        if (min.isUnknown()) return "?" + max;
        if (max.isUnknown()) return "?" + min;
        if (min.toString().equals(max.toString())) return "=" + min;
        return min + " - " + max;
    }
}
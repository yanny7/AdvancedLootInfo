package com.yanny.ali.api;

import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

public final class RangeValue {
    private float min;
    private float max;
    private boolean isRange = false;
    private boolean hasScore = false;
    private boolean isUnknown = false;

    public RangeValue() {
        this(1);
    }

    public RangeValue(RangeValue value) {
        min = value.min;
        max = value.max;
        isRange = value.isRange;
        hasScore = value.hasScore;
        isUnknown = value.isUnknown;
    }

    public RangeValue(boolean hasScore, boolean isUnknown) {
        this(1);
        this.hasScore = hasScore;
        this.isUnknown = isUnknown;
    }

    public RangeValue(float value) {
        min = max = value;
    }

    public RangeValue(float min, float max) {
        this.min = min;
        this.max = max;
        this.isRange = true;
    }

    public RangeValue(FriendlyByteBuf buf) {
        min = buf.readFloat();
        max = buf.readFloat();
        isRange = buf.readBoolean();
        hasScore = buf.readBoolean();
        isUnknown = buf.readBoolean();
    }

    public RangeValue multiply(float value) {
        this.min *= value;
        this.max *= value;
        return this;
    }

    public RangeValue multiply(RangeValue value) {
        this.min *= value.min;
        this.max *= value.max;
        this.isRange |= value.isRange;
        this.isUnknown |= value.isUnknown;
        return this;
    }

    public void multiplyMax(float value) {
        this.max *= value;
        this.isRange = true;
    }

    public void addMax(float value) {
        this.max += value;
        this.isRange = true;
    }

    public RangeValue add(float value) {
        min += value;
        max += value;
        return this;
    }

    public RangeValue add(RangeValue value) {
        if (isRange || value.isRange) {
            min += value.min;
            max += value.max;
        } else {
            min = max = min + value.min;
        }

        isRange |= value.isRange;
        hasScore |= value.hasScore;
        isUnknown |= value.isUnknown;
        return this;
    }

    public void set(RangeValue value) {
        min = value.min;
        max = value.max;
        isRange = value.isRange;
        hasScore = value.hasScore;
        isUnknown = value.isUnknown;
    }

    public float min() {
        return min;
    }

    public float max() {
        return max;
    }

    public boolean isRange() {
        return isRange;
    }

    public boolean isUnknown() {
        return isUnknown;
    }

    public RangeValue clamp(RangeValue min, RangeValue max) {
        if (this.min < min.min && !min.isUnknown && !isUnknown) {
            this.min = min.min;

            if (this.max < min.min) {
                this.max = min.min;
                this.isRange = false;
            }
        }
        if (this.max > max.max && !max.isUnknown && !isUnknown) {
            this.max = max.max;

            if (this.min > max.max) {
                this.min = max.max;
                this.isRange = false;
            }
        }

        return this;
    }

    @NotNull
    public String toIntString() {
        StringBuilder sb = new StringBuilder();

        if (isRange) {
            sb.append(Math.round(min)).append("-").append(Math.round(max));
        } else {
            sb.append(Math.round(min));
        }

        if (hasScore) {
            sb.append("[+Score]");
        }

        if (isUnknown) {
            sb.append("[+???]");
        }

        return sb.toString();
    }

    @NotNull
    public String toFloatString() {
        StringBuilder sb = new StringBuilder();

        if (isRange) {
            sb.append(format(min)).append("-").append(format(max));
        } else {
            sb.append(format(min));
        }

        if (hasScore) {
            sb.append("[+Score]");
        }

        if (isUnknown) {
            sb.append("[+???]");
        }

        return sb.toString();
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
        } else {
            return String.format("%.2f", value);
        }
    }

    @NotNull
    public static String rangeToString(RangeValue min, RangeValue max) {
        if (min.isUnknown()) {
            return "≤" + max;
        } else {
            if (max.isUnknown()) {
                return "≥" + min;
            } else {
                if (min.toString().equals(max.toString())) {
                    return "=" + min;
                } else {
                    return min + " - " + max;
                }
            }
        }
    }
}

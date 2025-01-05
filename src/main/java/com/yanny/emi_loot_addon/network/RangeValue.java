package com.yanny.emi_loot_addon.network;

import com.yanny.emi_loot_addon.mixin.MixinBinomialWithBonusCount;
import com.yanny.emi_loot_addon.mixin.MixinUniformGenerator;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    @NotNull
    public static RangeValue of(LootContext lootContext, @Nullable NumberProvider numberProvider) {
        try {
            if (numberProvider == null) {
                return new RangeValue(false, true);
            } else if (numberProvider.getType() == NumberProviders.UNIFORM) {
                MixinUniformGenerator uniformGenerator = (MixinUniformGenerator) numberProvider;
                return new RangeValue(of(lootContext, uniformGenerator.getMin()).min(), of(lootContext, uniformGenerator.getMax()).max());
            } else if (numberProvider.getType() == NumberProviders.BINOMIAL) {
                MixinBinomialWithBonusCount binomialGenerator = (MixinBinomialWithBonusCount) numberProvider;
                return new RangeValue(0, binomialGenerator.getExtraRounds());
            } else if (numberProvider.getType() == NumberProviders.CONSTANT) {
                return new RangeValue(numberProvider.getFloat(lootContext));
            } else if (numberProvider.getType() == NumberProviders.SCORE) {
                return new RangeValue(true, false);
            } else {
                return new RangeValue(false, true);
            }
        } catch (Exception e) {
            return new RangeValue();
        }
    }

    public RangeValue multiply(float value) {
        this.min *= value;
        this.max *= value;
        return this;
    }

    public void multiply(RangeValue value) {
        this.min *= value.min;
        this.max *= value.max;
        this.isRange |= value.isRange;
    }

    public void multiplyMax(float value) {
        this.max *= value;
        this.isRange = true;
    }

    public void addMax(float value) {
        this.max += value;
        this.isRange = true;
    }

    public void addMax(RangeValue value) {
        this.max += value.max;
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

    public boolean hasScore() {
        return hasScore;
    }

    public boolean isUnknown() {
        return isUnknown;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeFloat(min);
        buf.writeFloat(max);
        buf.writeBoolean(isRange);
        buf.writeBoolean(hasScore);
        buf.writeBoolean(isUnknown);
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

    public static String rangeToString(RangeValue min, RangeValue max) {
        if (min.isUnknown()) {
            return max.toString();
        } else {
            if (max.isUnknown()) {
                return min.toString();
            } else {
                if (min.toString().equals(max.toString())) {
                    return min.toString();
                } else {
                    return min + " - " + max;
                }
            }
        }
    }
}

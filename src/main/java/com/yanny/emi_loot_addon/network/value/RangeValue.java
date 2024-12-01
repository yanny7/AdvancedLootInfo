package com.yanny.emi_loot_addon.network.value;

public final class RangeValue {
    private float min;
    private float max;
    private boolean isRange;

    public RangeValue(float value) {
        min = max = value;
        isRange = false;
    }

    public RangeValue(float min, float max) {
        this.min = min;
        this.max = max;
        this.isRange = true;
    }

    public void multiplyMax(float value) {
        this.max *= value;
        this.isRange = true;
    }

    public void addMax(float value) {
        this.max += value;
        this.isRange = true;
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
}

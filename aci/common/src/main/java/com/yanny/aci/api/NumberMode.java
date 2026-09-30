package com.yanny.aci.api;

public record NumberMode(double lo, double hi, double probability) {
    public boolean hasProbability() {
        return !Double.isNaN(probability);
    }
}

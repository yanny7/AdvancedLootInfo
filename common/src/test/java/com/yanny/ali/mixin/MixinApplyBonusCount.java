package com.yanny.ali.mixin;

public interface MixinApplyBonusCount {
    interface UniformBonusCount {
        int getBonusMultiplier();
    }

    interface BinomialWithBonusCount {
        int getExtraRounds();

        float getProbability();
    }
}

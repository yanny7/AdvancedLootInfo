package com.yanny.ali.mixin;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ApplyBonusCount.class)
public interface MixinApplyBonusCount {
    @Accessor
    Holder<Enchantment> getEnchantment();

    @Accessor
    ApplyBonusCount.Formula getFormula();

    @Mixin(ApplyBonusCount.BinomialWithBonusCount.class)
    interface BinomialWithBonusCount {
        @Accessor
        int getExtraRounds();

        @Accessor
        float getProbability();
    }

    @Mixin(ApplyBonusCount.UniformBonusCount.class)
    interface UniformBonusCount {
        @Accessor
        int getBonusMultiplier();
    }

    @Accessor("FORMULA_CODEC")
    static MapCodec<ApplyBonusCount.Formula> invokeFormulaCodec() {
        throw new AssertionError();
    }
}

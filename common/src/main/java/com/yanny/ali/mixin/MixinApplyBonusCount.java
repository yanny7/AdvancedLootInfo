package com.yanny.ali.mixin;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(ApplyBonusCount.class)
public interface MixinApplyBonusCount {
    @Accessor
    Enchantment getEnchantment();

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

    @Accessor("FORMULAS")
    static Map<ResourceLocation, ApplyBonusCount.FormulaDeserializer> invokeFormulas() {
        throw new AssertionError();
    }
}

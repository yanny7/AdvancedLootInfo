package com.yanny.ali.mixin;

import com.almostreliable.lootjs.loot.condition.MatchFluid;
import net.minecraft.advancements.critereon.FluidPredicate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MatchFluid.class)
public interface MixinMatchFluid {
    @Accessor
    FluidPredicate getPredicate();
}

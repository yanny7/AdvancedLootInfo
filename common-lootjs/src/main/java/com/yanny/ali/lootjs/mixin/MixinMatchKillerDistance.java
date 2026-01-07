package com.yanny.ali.lootjs.mixin;

import com.almostreliable.lootjs.loot.condition.MatchKillerDistance;
import net.minecraft.advancements.critereon.DistancePredicate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MatchKillerDistance.class)
public interface MixinMatchKillerDistance {
    @Accessor
    DistancePredicate getPredicate();
}

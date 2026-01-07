package com.yanny.ali.lootjs.mixin;

import com.almostreliable.lootjs.loot.condition.MatchPlayer;
import net.minecraft.advancements.critereon.EntityPredicate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MatchPlayer.class)
public interface MixinMatchPlayer {
    @Accessor
    EntityPredicate getPredicate();
}

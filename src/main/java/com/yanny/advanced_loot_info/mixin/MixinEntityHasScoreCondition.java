package com.yanny.advanced_loot_info.mixin;

import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.EntityHasScoreCondition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(EntityHasScoreCondition.class)
public interface MixinEntityHasScoreCondition {
    @Accessor
    Map<String, IntRange> getScores();

    @Accessor
    LootContext.EntityTarget getEntityTarget();
}

package com.yanny.emi_loot_addon.mixin;

import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MatchTool.class)
public interface MixinMatchTool {
    @Accessor
    ItemPredicate getPredicate();
}

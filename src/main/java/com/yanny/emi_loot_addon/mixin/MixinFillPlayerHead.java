package com.yanny.emi_loot_addon.mixin;

import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.FillPlayerHead;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(FillPlayerHead.class)
public interface MixinFillPlayerHead {
    @Accessor
    LootContext.EntityTarget getEntityTarget();
}

package com.yanny.advanced_loot_info.mixin;

import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.FillPlayerHead;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(FillPlayerHead.class)
public interface MixinFillPlayerHead {
    @Accessor
    LootContext.EntityTarget getEntityTarget();
}

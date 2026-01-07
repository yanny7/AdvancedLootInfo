package com.yanny.ali.lootjs.mixin;

import com.almostreliable.lootjs.core.ILootHandler;
import com.almostreliable.lootjs.loot.action.CompositeLootAction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Collection;

@Mixin(CompositeLootAction.class)
public interface MixinCompositeLootAction {
    @Accessor
    Collection<ILootHandler> getHandlers();
}

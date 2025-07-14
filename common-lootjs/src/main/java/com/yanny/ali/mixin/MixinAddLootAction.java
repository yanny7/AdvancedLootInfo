package com.yanny.ali.mixin;

import com.almostreliable.lootjs.core.LootEntry;
import com.almostreliable.lootjs.loot.action.AddLootAction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AddLootAction.class)
public interface MixinAddLootAction {
    @Accessor
    LootEntry[] getEntries();

    @Accessor
    AddLootAction.AddType getType();
}

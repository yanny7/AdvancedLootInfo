package com.yanny.ali.mixin;

import com.almostreliable.lootjs.loot.modifier.handler.AddLootAction;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AddLootAction.class)
public interface MixinAddLootAction {
    @Accessor
    LootPoolEntryContainer[] getEntries();
}

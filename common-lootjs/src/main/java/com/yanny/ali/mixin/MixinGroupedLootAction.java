package com.yanny.ali.mixin;

import com.almostreliable.lootjs.loot.action.GroupedLootAction;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GroupedLootAction.class)
public interface MixinGroupedLootAction {
    @Accessor
    NumberProvider getNumberProvider();
}

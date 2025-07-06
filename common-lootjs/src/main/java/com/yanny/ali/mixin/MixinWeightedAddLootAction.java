package com.yanny.ali.mixin;

import com.almostreliable.lootjs.core.LootEntry;
import com.almostreliable.lootjs.loot.action.WeightedAddLootAction;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(WeightedAddLootAction.class)
public interface MixinWeightedAddLootAction {
    @Accessor
    NumberProvider getNumberProvider();

    @Accessor
    SimpleWeightedRandomList<LootEntry> getWeightedRandomList();

    @Accessor
    boolean getAllowDuplicateLoot();
}

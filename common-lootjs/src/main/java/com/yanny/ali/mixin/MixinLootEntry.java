package com.yanny.ali.mixin;

import com.almostreliable.lootjs.core.LootEntry;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(LootEntry.class)
public interface MixinLootEntry {
    @Accessor
    LootEntry.Generator getGenerator();

    @Accessor
    List<LootItemFunction> getPostModifications();

    @Accessor
    List<LootItemCondition> getConditions();

    @Accessor
    int getWeight();
}

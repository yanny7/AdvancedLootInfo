package com.yanny.ali.mixin;

import com.almostreliable.lootjs.core.entry.AbstractSimpleLootEntry;
import com.almostreliable.lootjs.loot.LootConditionList;
import com.almostreliable.lootjs.loot.LootFunctionList;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractSimpleLootEntry.class)
public interface MixinAbstractSimpleLootEntry<E extends LootPoolSingletonContainer> {
    @Accessor
    E getVanillaEntry();

    @Accessor
    LootConditionList getConditions();

    @Accessor
    LootFunctionList getFunctions();
}

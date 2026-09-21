package com.yanny.ali.neoforge.mixin;

import net.minecraft.core.Holder;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.LootModifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Optional;

@Mixin(LootModifier.class)
public interface MixinLootModifier {
    @Accessor("condition")
    Optional<Holder<LootItemCondition>> getAliCondition();
}

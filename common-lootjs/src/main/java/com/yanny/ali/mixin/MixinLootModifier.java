package com.yanny.ali.mixin;

import com.almostreliable.lootjs.loot.modifier.LootModifier;
import net.minecraft.world.level.storage.loot.LootContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.function.Predicate;

@Mixin(LootModifier.class)
public interface MixinLootModifier {
    @Accessor
    Predicate<LootContext> getShouldRun();
}

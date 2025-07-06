package com.yanny.ali.mixin;

import com.almostreliable.lootjs.core.LootEntry;
import com.almostreliable.lootjs.loot.action.ReplaceLootAction;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.function.Predicate;

@Mixin(ReplaceLootAction.class)
public interface MixinReplaceLootAction {
    @Accessor
    Predicate<ItemStack> getPredicate();

    @Accessor
    LootEntry getLootEntry();

    @Accessor
    boolean getPreserveCount();
}

package com.yanny.ali.lootjs.mixin;

import com.almostreliable.lootjs.loot.action.RemoveLootAction;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.function.Predicate;

@Mixin(RemoveLootAction.class)
public interface MixinRemoveLootAction {
    @Accessor
    Predicate<ItemStack> getPredicate();
}

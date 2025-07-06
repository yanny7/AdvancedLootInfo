package com.yanny.ali.mixin;

import com.almostreliable.lootjs.loot.action.ModifyLootAction;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.function.Predicate;

@Mixin(ModifyLootAction.class)
public interface MixinModifyLootAction {
    @Accessor
    Predicate<ItemStack> getPredicate();

    @Accessor
    ModifyLootAction.Callback getCallback();
}

package com.yanny.ali.mixin;

import com.almostreliable.lootjs.loot.condition.ContainsLootCondition;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.function.Predicate;

@Mixin(ContainsLootCondition.class)
public interface MixinContainsLootCondition {
    @Accessor
    Predicate<ItemStack> getPredicate();

    @Accessor
    boolean getExact();
}

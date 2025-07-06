package com.yanny.ali.mixin;

import com.almostreliable.lootjs.loot.condition.MatchEquipmentSlot;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.function.Predicate;

@Mixin(MatchEquipmentSlot.class)
public interface MixinMatchEquipmentSlot {
    @Accessor
    Predicate<ItemStack> getPredicate();

    @Accessor
    EquipmentSlot getSlot();
}

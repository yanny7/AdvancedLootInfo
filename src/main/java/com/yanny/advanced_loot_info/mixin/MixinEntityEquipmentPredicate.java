package com.yanny.advanced_loot_info.mixin;

import net.minecraft.advancements.critereon.EntityEquipmentPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntityEquipmentPredicate.class)
public interface MixinEntityEquipmentPredicate {
    @Accessor
    ItemPredicate getHead();

    @Accessor
    ItemPredicate getChest();

    @Accessor
    ItemPredicate getLegs();

    @Accessor
    ItemPredicate getFeet();

    @Accessor
    ItemPredicate getMainhand();

    @Accessor
    ItemPredicate getOffhand();
}

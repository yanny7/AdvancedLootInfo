package com.yanny.ali.mixin;

import net.minecraft.advancements.critereon.ItemPredicate;

public interface MixinEntityEquipmentPredicate {
    ItemPredicate getHead();
    ItemPredicate getChest();
    ItemPredicate getLegs();
    ItemPredicate getFeet();
    ItemPredicate getMainhand();
    ItemPredicate getOffhand();
}

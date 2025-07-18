package com.yanny.ali.mixin;

import com.almostreliable.lootjs.core.filters.ItemFilter;
import com.almostreliable.lootjs.loot.condition.MatchEquipmentSlot;
import net.minecraft.world.entity.EquipmentSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MatchEquipmentSlot.class)
public interface MixinMatchEquipmentSlot {
    @Accessor
    ItemFilter getItemFilter();

    @Accessor
    EquipmentSlot getSlot();
}

package com.yanny.ali.mixin;

import net.neoforged.neoforge.common.ItemAbility;
import net.neoforged.neoforge.common.loot.CanItemPerformAbility;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = CanItemPerformAbility.class, remap = false)
public interface MixinCanItemPerformAbility {
    @Accessor
    ItemAbility getAbility();
}

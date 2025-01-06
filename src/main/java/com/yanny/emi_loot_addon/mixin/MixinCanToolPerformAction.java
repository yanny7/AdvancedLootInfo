package com.yanny.emi_loot_addon.mixin;

import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.loot.CanToolPerformAction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = CanToolPerformAction.class, remap = false)
public interface MixinCanToolPerformAction {
    @Accessor
    ToolAction getAction();
}

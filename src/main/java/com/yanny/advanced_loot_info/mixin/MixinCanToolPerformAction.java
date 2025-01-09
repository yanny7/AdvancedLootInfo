package com.yanny.advanced_loot_info.mixin;

import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.loot.CanToolPerformAction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = CanToolPerformAction.class, remap = false)
public interface MixinCanToolPerformAction {
    @Accessor
    ToolAction getAction();
}

package com.yanny.ali.mixin;

import net.neoforged.neoforge.common.ToolAction;
import net.neoforged.neoforge.common.loot.CanToolPerformAction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = CanToolPerformAction.class, remap = false)
public interface MixinCanToolPerformAction {
    @Accessor
    ToolAction getAction();
}

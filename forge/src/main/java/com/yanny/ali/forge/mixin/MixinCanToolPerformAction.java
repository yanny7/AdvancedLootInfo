package com.yanny.ali.forge.mixin;

import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.loot.CanToolPerformAction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CanToolPerformAction.class)
public interface MixinCanToolPerformAction {
    @Accessor
    ToolAction getAction();
}

package com.yanny.advanced_loot_info.mixin;

import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.SetNameFunction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SetNameFunction.class)
public interface MixinSetNameFunction {
    @Accessor
    Component getName();

    @Accessor
    LootContext.EntityTarget getResolutionContext();
}

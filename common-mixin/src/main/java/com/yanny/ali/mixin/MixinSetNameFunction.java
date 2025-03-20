package com.yanny.ali.mixin;

import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.SetNameFunction;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SetNameFunction.class)
public interface MixinSetNameFunction {
    @Accessor
    Component getName();

    @Nullable
    @Accessor
    LootContext.EntityTarget getResolutionContext();
}

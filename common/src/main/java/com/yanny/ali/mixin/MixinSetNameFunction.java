package com.yanny.ali.mixin;

import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.SetNameFunction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Optional;

@Mixin(SetNameFunction.class)
public interface MixinSetNameFunction {
    @Accessor
    Optional<Component> getName();

    @Accessor
    Optional<LootContext.EntityTarget> getResolutionContext();
}

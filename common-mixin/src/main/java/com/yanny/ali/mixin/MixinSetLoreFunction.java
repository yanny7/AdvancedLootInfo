package com.yanny.ali.mixin;

import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.SetLoreFunction;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(SetLoreFunction.class)
public interface MixinSetLoreFunction {
    @Accessor
    boolean getReplace();

    @Accessor
    List<Component> getLore();

    @Nullable
    @Accessor
    LootContext.EntityTarget getResolutionContext();
}

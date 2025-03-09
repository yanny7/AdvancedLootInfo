package com.yanny.ali.mixin;

import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.ListOperation;
import net.minecraft.world.level.storage.loot.functions.SetLoreFunction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;
import java.util.Optional;

@Mixin(SetLoreFunction.class)
public interface MixinSetLoreFunction {
    @Accessor
    ListOperation getMode();

    @Accessor
    List<Component> getLore();

    @Accessor
    Optional<LootContext.EntityTarget> getResolutionContext();
}

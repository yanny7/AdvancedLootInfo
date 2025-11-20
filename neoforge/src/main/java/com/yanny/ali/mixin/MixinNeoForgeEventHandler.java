package com.yanny.ali.mixin;

import net.neoforged.neoforge.common.NeoForgeEventHandler;
import net.neoforged.neoforge.common.loot.LootModifierManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(NeoForgeEventHandler.class)
public interface MixinNeoForgeEventHandler {
    @Invoker(value = "getLootModifierManager")
    static LootModifierManager getLootModifierManager() {
        throw new IllegalStateException();
    }
}
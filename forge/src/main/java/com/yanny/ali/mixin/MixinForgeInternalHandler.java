package com.yanny.ali.mixin;

import net.minecraftforge.common.ForgeInternalHandler;
import net.minecraftforge.common.loot.LootModifierManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ForgeInternalHandler.class)
public interface MixinForgeInternalHandler {
    @Invoker(value = "getLootModifierManager")
    static LootModifierManager getLootModifierManager() {
        throw new IllegalStateException();
    }
}
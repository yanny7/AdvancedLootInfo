package com.yanny.advanced_loot_info.mixin;

import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.SetLoreFunction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(SetLoreFunction.class)
public interface MixinSetLoreFunction {
    @Accessor
    boolean getReplace();

    @Accessor
    List<Component> getLore();

    @Accessor
    LootContext.EntityTarget getResolutionContext();
}

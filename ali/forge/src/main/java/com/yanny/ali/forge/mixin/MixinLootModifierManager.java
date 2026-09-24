package com.yanny.ali.forge.mixin;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifierManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(value = LootModifierManager.class, remap = false)
public interface MixinLootModifierManager {
    @Accessor("modifiers")
    Map<ResourceLocation, IGlobalLootModifier> getAliRegisteredLootModifiers();
}

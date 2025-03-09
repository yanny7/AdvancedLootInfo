package com.yanny.ali.mixin;

import net.minecraft.world.level.storage.loot.ContainerComponentManipulator;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.ModifyContainerContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ModifyContainerContents.class)
public interface MixinModifyContainerContents {
    @Accessor
    ContainerComponentManipulator<?> getComponent();

    @Accessor
    LootItemFunction getModifier();
}

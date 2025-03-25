package com.yanny.ali.mixin;

import net.minecraft.world.level.storage.loot.ContainerComponentManipulator;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.SetContainerContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(SetContainerContents.class)
public interface MixinSetContainerContents {
    @Accessor
    ContainerComponentManipulator<?> getComponent();

    @Accessor
    List<LootPoolEntryContainer> getEntries();
}

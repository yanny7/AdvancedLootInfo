package com.yanny.ali.mixin;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.level.storage.loot.functions.CopyComponentsFunction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;
import java.util.Optional;

@Mixin(CopyComponentsFunction.class)
public interface MixinCopyComponentsFunction {
    @Accessor
    CopyComponentsFunction.Source getSource();

    @Accessor
    Optional<List<DataComponentType<?>>> getInclude();

    @Accessor
    Optional<List<DataComponentType<?>>> getExclude();
}

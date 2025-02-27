package com.yanny.ali.mixin;

import net.minecraft.world.level.storage.loot.functions.SetAttributesFunction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(SetAttributesFunction.class)
public interface MixinSetAttributesFunction {
    @Accessor
    List<SetAttributesFunction.Modifier> getModifiers();
}

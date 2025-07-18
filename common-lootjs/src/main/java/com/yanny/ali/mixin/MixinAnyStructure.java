package com.yanny.ali.mixin;

import com.almostreliable.lootjs.loot.condition.AnyStructure;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(AnyStructure.class)
public interface MixinAnyStructure {
    @Accessor
    List<AnyStructure.StructureLocator> getStructureLocators();

    @Accessor
    boolean getExact();
}

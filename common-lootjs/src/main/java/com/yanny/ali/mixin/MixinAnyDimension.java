package com.yanny.ali.mixin;

import com.almostreliable.lootjs.loot.condition.AnyDimension;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AnyDimension.class)
public interface MixinAnyDimension {
    @Accessor
    ResourceLocation[] getDimensions();
}

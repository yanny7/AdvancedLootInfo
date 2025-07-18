package com.yanny.ali.mixin;

import com.almostreliable.lootjs.loot.condition.MatchDimension;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MatchDimension.class)
public interface MixinMatchDimension {
    @Accessor
    ResourceLocation[] getDimensions();
}

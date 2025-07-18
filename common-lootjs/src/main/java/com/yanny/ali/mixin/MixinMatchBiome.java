package com.yanny.ali.mixin;

import com.almostreliable.lootjs.loot.condition.MatchBiome;
import net.minecraft.core.HolderSet;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MatchBiome.class)
public interface MixinMatchBiome {
    @Accessor
    HolderSet<Biome> getBiomes();
}

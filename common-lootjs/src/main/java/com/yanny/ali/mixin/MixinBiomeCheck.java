package com.yanny.ali.mixin;

import com.almostreliable.lootjs.loot.condition.BiomeCheck;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(BiomeCheck.class)
public interface MixinBiomeCheck {
    @Accessor
    List<ResourceKey<Biome>> getBiomes();

    @Accessor
    List<TagKey<Biome>> getTags();
}

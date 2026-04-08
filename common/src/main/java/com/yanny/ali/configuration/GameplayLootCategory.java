package com.yanny.ali.configuration;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;
import java.util.regex.Pattern;

public class GameplayLootCategory extends LootCategory<ResourceLocation> {
    private static final Codec<Pattern> PATTERN_CODEC = Codec.STRING.xmap(
            Pattern::compile,
            Pattern::pattern
    );
    public static final MapCodec<GameplayLootCategory> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    ResourceLocation.CODEC.fieldOf("key").forGetter(LootCategory::getKey),
                    BuiltInRegistries.ITEM.byNameCodec().fieldOf("icon").forGetter(LootCategory::getIcon),
                    Codec.BOOL.optionalFieldOf("hide", false).forGetter(LootCategory::isHidden),
                    Ingredient.CODEC.optionalFieldOf("catalyst", Ingredient.EMPTY).forGetter(LootCategory::getCatalyst),
                    PATTERN_CODEC.listOf().fieldOf("pattern").forGetter(src -> src.patterns)
            ).apply(instance, GameplayLootCategory::new)
    );

    private final List<Pattern> patterns;

    public GameplayLootCategory(ResourceLocation key, Item icon, boolean hide, Ingredient catalyst, List<Pattern> patterns) {
        super(key, icon, Type.GAMEPLAY, hide, catalyst);
        this.patterns = patterns;
    }

    @Override
    public boolean validate(ResourceLocation path) {
        return patterns.stream().anyMatch((p) -> p.matcher(path.toString()).find());
    }
}

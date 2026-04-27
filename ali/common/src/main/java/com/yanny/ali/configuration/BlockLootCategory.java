package com.yanny.ali.configuration;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;

import java.util.Collections;
import java.util.List;

public class BlockLootCategory extends LootCategory<Block> {
    private static final Codec<TagKey<Block>> BLOCK_TAG_CODEC = TagKey.hashedCodec(Registries.BLOCK);
    private static final Codec<Block> BLOCK_CODEC = BuiltInRegistries.BLOCK.byNameCodec();
    private static final Codec<Either<TagKey<Block>, Block>> TAG_OR_BLOCK_CODEC = Codec.either(BLOCK_TAG_CODEC, BLOCK_CODEC);
    public static final MapCodec<BlockLootCategory> CODEC = RecordCodecBuilder.mapCodec((instance) ->
        instance.group(
                Identifier.CODEC.fieldOf("key").forGetter(LootCategory::getKey),
                BuiltInRegistries.ITEM.byNameCodec().fieldOf("icon").forGetter(LootCategory::getIcon),
                Codec.BOOL.optionalFieldOf("hide", false).forGetter(LootCategory::isHidden),
                Ingredient.CODEC.listOf().optionalFieldOf("catalysts", Collections.emptyList()).forGetter(LootCategory::getCatalysts),
                TAG_OR_BLOCK_CODEC.listOf().fieldOf("blocks").forGetter(src -> src.blocks)
        ).apply(instance, BlockLootCategory::new)
    );

    private final List<Either<TagKey<Block>, Block>> blocks;

    public BlockLootCategory(Identifier key, Item icon, boolean hide, List<Ingredient> catalysts, List<Either<TagKey<Block>, Block>> blocks) {
        super(key, icon, Type.BLOCK, hide, catalysts);
        this.blocks = blocks;
    }

    @Override
    public boolean validate(Block block) {
        if (blocks.isEmpty()) {
            return true;
        }

        return blocks.stream().anyMatch((either) -> either.map(
                (tag) -> block.builtInRegistryHolder().is(tag),
                (bl) -> bl == block
        ));
    }
}

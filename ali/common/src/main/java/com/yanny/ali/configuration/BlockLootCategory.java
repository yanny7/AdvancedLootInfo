package com.yanny.ali.configuration;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Either;
import com.yanny.aci.CommonLogUtils;
import com.yanny.ali.Utils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import org.slf4j.Logger;

import java.util.List;
import java.util.Objects;

public class BlockLootCategory extends LootCategory<Block> {
    private static final Logger LOGGER = CommonLogUtils.getLogger(Utils.MOD_ID);

    private final List<Either<TagKey<Block>, Block>> blocks;

    public BlockLootCategory(ResourceLocation key, Item icon, boolean hide, List<Ingredient> catalysts, List<Either<TagKey<Block>, Block>> blocks) {
        super(key, icon, Type.BLOCK, hide, catalysts);
        this.blocks = blocks;
    }

    public BlockLootCategory(JsonObject object) {
        super(Type.BLOCK, object);
        blocks = GsonHelper.getAsJsonArray(object, "blocks")
                .asList()
                .stream()
                .map(JsonElement::getAsString)
                .map((s) -> {
                    if (s.startsWith("#")) {
                        ResourceLocation location = ResourceLocation.tryParse(s.substring(1));

                        if (location != null) {
                            return Either.<TagKey<Block>, Block>left(TagKey.create(Registries.BLOCK, location));
                        }
                    } else {
                        ResourceLocation location = ResourceLocation.tryParse(s);

                        if (location != null && BuiltInRegistries.BLOCK.containsKey(location)) {
                            return Either.<TagKey<Block>, Block>right(BuiltInRegistries.BLOCK.get(location));
                        }
                    }

                    LOGGER.warn("Ignoring invalid entry '{}' in 'blocks' of category {}", s, getKey());
                    return null;
                })
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    protected void toJson(JsonObject object) {
        JsonArray array = new JsonArray();

        for (Either<TagKey<Block>, Block> entry : blocks) {
            entry.ifLeft((tag) -> array.add("#" + tag.location()));
            entry.ifRight((block) -> array.add(BuiltInRegistries.BLOCK.getKey(block).toString()));
        }

        object.add("blocks", array);
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

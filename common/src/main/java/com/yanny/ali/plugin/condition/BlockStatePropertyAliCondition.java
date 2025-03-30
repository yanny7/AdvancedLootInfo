package com.yanny.ali.plugin.condition;

import com.yanny.ali.api.IContext;
import com.yanny.ali.api.ILootCondition;
import com.yanny.ali.mixin.MixinLootItemBlockStatePropertyCondition;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class BlockStatePropertyAliCondition implements ILootCondition {
    public final Block block;
    public final StatePropertiesPredicate properties;

    public BlockStatePropertyAliCondition(IContext context, LootItemCondition condition) {
        block = ((MixinLootItemBlockStatePropertyCondition) condition).getBlock();
        properties = ((MixinLootItemBlockStatePropertyCondition) condition).getProperties();
    }

    public BlockStatePropertyAliCondition(IContext context, FriendlyByteBuf buf) {
        block = BuiltInRegistries.BLOCK.get(buf.readResourceLocation());
        properties = StatePropertiesPredicate.fromJson(buf.readJsonWithCodec(ExtraCodecs.JSON));
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        buf.writeResourceLocation(BuiltInRegistries.BLOCK.getKey(block));
        buf.writeJsonWithCodec(ExtraCodecs.JSON, properties.serializeToJson());
    }
}

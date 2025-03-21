package com.yanny.ali.plugin.condition;

import com.google.gson.JsonElement;
import com.yanny.ali.api.IContext;
import com.yanny.ali.api.ILootCondition;
import com.yanny.ali.plugin.ConditionTooltipUtils;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.List;
import java.util.Optional;

public class BlockStatePropertyAliCondition implements ILootCondition {
    public final Holder<Block> block;
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public final Optional<StatePropertiesPredicate> properties;

    public BlockStatePropertyAliCondition(IContext context, LootItemCondition condition) {
        block = ((LootItemBlockStatePropertyCondition) condition).block();
        properties = ((LootItemBlockStatePropertyCondition) condition).properties();
    }

    public BlockStatePropertyAliCondition(IContext context, FriendlyByteBuf buf) {
        block = BuiltInRegistries.BLOCK.getHolderOrThrow(buf.readResourceKey(Registries.BLOCK));
        Optional<JsonElement> jsonElement = buf.readOptional((a) -> a.readJsonWithCodec(ExtraCodecs.JSON));
        properties = jsonElement.flatMap(StatePropertiesPredicate::fromJson);
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        buf.writeResourceKey(block.unwrapKey().orElseThrow());
        buf.writeOptional(properties, (b, v) -> b.writeJsonWithCodec(ExtraCodecs.JSON, v.serializeToJson()));
    }

    @Override
    public List<Component> getTooltip(int pad) {
        return ConditionTooltipUtils.getBlockStatePropertyTooltip(pad, block, properties);
    }
}

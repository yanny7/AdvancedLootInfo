package com.yanny.ali.plugin.condition;

import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.JsonOps;
import com.yanny.ali.api.IContext;
import com.yanny.ali.api.ILootCondition;
import com.yanny.ali.plugin.TooltipUtils;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static com.yanny.ali.plugin.TooltipUtils.translatable;

public class BlockStatePropertyCondition implements ILootCondition {
    public final ResourceLocation block;
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public final Optional<StatePropertiesPredicate> properties;

    public BlockStatePropertyCondition(IContext context, LootItemCondition condition) {
        block = BuiltInRegistries.BLOCK.getKey(((LootItemBlockStatePropertyCondition) condition).block().value());
        properties = ((LootItemBlockStatePropertyCondition) condition).properties();
    }

    public BlockStatePropertyCondition(IContext context, FriendlyByteBuf buf) {
        block = buf.readResourceLocation();
        Optional<JsonElement> jsonElement = buf.readOptional((a) -> a.readJsonWithCodec(ExtraCodecs.JSON));
        properties = jsonElement.flatMap((e) -> StatePropertiesPredicate.CODEC.decode(JsonOps.INSTANCE, e).result()).map(Pair::getFirst);
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        buf.writeResourceLocation(block);
        buf.writeOptional(properties.flatMap((v) -> StatePropertiesPredicate.CODEC.encodeStart(JsonOps.INSTANCE, v).result()),
                (b, v) -> b.writeJsonWithCodec(ExtraCodecs.JSON, v));
    }

    @Override
    public List<Component> getTooltip(int pad) {
        List<Component> components = new LinkedList<>();

        properties.ifPresent((p) -> TooltipUtils.addStateProperties(components, pad, translatable("ali.type.condition.block_state_property"), p));

        return components;
    }
}

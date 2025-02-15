package com.yanny.ali.plugin.condition;

import com.yanny.ali.api.IContext;
import com.yanny.ali.api.ILootCondition;
import com.yanny.ali.mixin.MixinLootItemBlockStatePropertyCondition;
import com.yanny.ali.plugin.TooltipUtils;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.LinkedList;
import java.util.List;

import static com.yanny.ali.plugin.TooltipUtils.translatable;

public class BlockStatePropertyCondition implements ILootCondition {
    public final ResourceLocation block;
    public final StatePropertiesPredicate properties;

    public BlockStatePropertyCondition(IContext context, LootItemCondition condition) {
        block = BuiltInRegistries.BLOCK.getKey(((MixinLootItemBlockStatePropertyCondition) condition).getBlock());
        properties = ((MixinLootItemBlockStatePropertyCondition) condition).getProperties();
    }

    public BlockStatePropertyCondition(IContext context, FriendlyByteBuf buf) {
        block = buf.readResourceLocation();
        properties = StatePropertiesPredicate.fromJson(buf.readJsonWithCodec(ExtraCodecs.JSON));
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        buf.writeResourceLocation(block);
        buf.writeJsonWithCodec(ExtraCodecs.JSON, properties.serializeToJson());
    }

    @Override
    public List<Component> getTooltip(int pad) {
        List<Component> components = new LinkedList<>();

        TooltipUtils.addStateProperties(components, pad, translatable("ali.type.condition.block_state_property"), properties);

        return components;
    }
}

package com.yanny.advanced_loot_info.plugin.condition;

import com.yanny.advanced_loot_info.api.IContext;
import com.yanny.advanced_loot_info.api.ILootCondition;
import com.yanny.advanced_loot_info.mixin.MixinLootItemBlockStatePropertyCondition;
import com.yanny.advanced_loot_info.plugin.TooltipUtils;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.LinkedList;
import java.util.List;

import static com.yanny.advanced_loot_info.compatibility.emi.EmiUtils.translatable;

public class BlockStatePropertyCondition implements ILootCondition {
    public final ResourceLocation block;
    public final StatePropertiesPredicate properties;

    public BlockStatePropertyCondition(IContext context, LootItemCondition condition) {
        block = ForgeRegistries.BLOCKS.getKey(((MixinLootItemBlockStatePropertyCondition) condition).getBlock());
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

        TooltipUtils.addStateProperties(components, pad, translatable("emi.type.advanced_loot_info.condition.block_state_property"), properties);

        return components;
    }
}

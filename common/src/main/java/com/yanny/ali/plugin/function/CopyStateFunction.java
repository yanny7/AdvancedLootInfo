package com.yanny.ali.plugin.function;

import com.yanny.ali.api.IContext;
import com.yanny.ali.mixin.MixinCopyBlockState;
import com.yanny.ali.plugin.TooltipUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class CopyStateFunction extends LootConditionalFunction {
    public final Block block;
    public final Set<Property<?>> properties;

    public CopyStateFunction(IContext context, LootItemFunction function) {
        super(context, function);
        block = ((MixinCopyBlockState) function).getBlock().value();
        properties = ((MixinCopyBlockState) function).getProperties();
    }

    public CopyStateFunction(IContext context, FriendlyByteBuf buf) {
        super(context, buf);
        block = BuiltInRegistries.BLOCK.get(buf.readResourceLocation());

        int count = buf.readInt();
        StateDefinition<Block, BlockState> stateDefinition = block.getStateDefinition();

        properties = new HashSet<>();

        for (int i = 0; i < count; i++) {
            properties.add(stateDefinition.getProperty(buf.readUtf()));
        }
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        super.encode(context, buf);
        buf.writeResourceLocation(BuiltInRegistries.BLOCK.getKey(block));
        buf.writeInt(properties.size());
        properties.forEach((property) -> buf.writeUtf(property.getName()));
    }

    @Override
    public List<Component> getTooltip(int pad) {
        List<Component> components = new LinkedList<>();

        components.add(TooltipUtils.pad(pad, TooltipUtils.translatable("ali.type.function.copy_state")));
        components.add(TooltipUtils.pad(pad + 1, TooltipUtils.translatable("ali.property.function.copy_state.block", TooltipUtils.value(TooltipUtils.translatable(block.getDescriptionId())))));

        if (!properties.isEmpty()) {
            components.add(TooltipUtils.pad(pad + 1, TooltipUtils.translatable("ali.property.function.copy_state.properties", properties)));
            properties.forEach((property) -> components.add(TooltipUtils.pad(pad + 2, TooltipUtils.value(property.getName()))));
        }

        return components;
    }
}

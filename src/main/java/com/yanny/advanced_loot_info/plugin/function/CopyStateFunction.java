package com.yanny.advanced_loot_info.plugin.function;

import com.yanny.advanced_loot_info.api.IContext;
import com.yanny.advanced_loot_info.mixin.MixinCopyBlockState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static com.yanny.advanced_loot_info.compatibility.EmiUtils.*;

public class CopyStateFunction extends LootConditionalFunction {
    public final Block block;
    public final Set<Property<?>> properties;

    public CopyStateFunction(IContext context, LootItemFunction function) {
        super(context, function);
        block = ((MixinCopyBlockState) function).getBlock();
        properties = ((MixinCopyBlockState) function).getProperties();
    }

    public CopyStateFunction(IContext context, FriendlyByteBuf buf) {
        super(context, buf);
        block = ForgeRegistries.BLOCKS.getValue(buf.readResourceLocation());

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
        buf.writeResourceLocation(ForgeRegistries.BLOCKS.getKey(block));
        buf.writeInt(properties.size());
        properties.forEach((property) -> buf.writeUtf(property.getName()));
    }

    @Override
    public List<Component> getTooltip(int pad) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("emi.type.advanced_loot_info.function.copy_state")));
        components.add(pad(pad + 1, translatable("emi.property.function.copy_state.block", value(translatable(block.getDescriptionId())))));

        if (!properties.isEmpty()) {
            components.add(pad(pad + 1, translatable("emi.property.function.copy_state.properties", properties)));
            properties.forEach((property) -> components.add(pad(pad + 2, value(property.getName()))));
        }

        return components;
    }
}

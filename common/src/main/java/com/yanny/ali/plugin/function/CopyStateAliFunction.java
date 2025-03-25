package com.yanny.ali.plugin.function;

import com.yanny.ali.api.IContext;
import com.yanny.ali.mixin.MixinCopyBlockState;
import com.yanny.ali.plugin.FunctionTooltipUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CopyStateAliFunction extends LootConditionalAliFunction {
    public final Holder<Block> block;
    public final Set<Property<?>> properties;

    public CopyStateAliFunction(IContext context, LootItemFunction function) {
        super(context, function);
        block = ((MixinCopyBlockState) function).getBlock();
        properties = ((MixinCopyBlockState) function).getProperties();
    }

    public CopyStateAliFunction(IContext context, FriendlyByteBuf buf) {
        super(context, buf);
        block = BuiltInRegistries.BLOCK.getHolderOrThrow(buf.readResourceKey(Registries.BLOCK));

        int count = buf.readInt();
        StateDefinition<Block, BlockState> stateDefinition = block.value().getStateDefinition();

        properties = new HashSet<>();

        for (int i = 0; i < count; i++) {
            properties.add(stateDefinition.getProperty(buf.readUtf()));
        }
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        super.encode(context, buf);
        buf.writeResourceKey(block.unwrap().orThrow());
        buf.writeInt(properties.size());
        properties.forEach((property) -> buf.writeUtf(property.getName()));
    }

    @Override
    public List<Component> getTooltip(int pad) {
        return FunctionTooltipUtils.getCopyStateTooltip(pad, block, properties);
    }
}

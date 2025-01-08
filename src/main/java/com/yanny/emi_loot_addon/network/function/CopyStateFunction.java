package com.yanny.emi_loot_addon.network.function;

import com.yanny.emi_loot_addon.mixin.MixinCopyBlockState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashSet;
import java.util.Set;

public class CopyStateFunction extends LootConditionalFunction {
    public final Block block;
    public final Set<Property<?>> properties;

    public CopyStateFunction(LootContext lootContext, LootItemFunction function) {
        super(lootContext, function);
        block = ((MixinCopyBlockState) function).getBlock();
        properties = ((MixinCopyBlockState) function).getProperties();
    }

    public CopyStateFunction(FunctionType type, FriendlyByteBuf buf) {
        super(type, buf);
        block = ForgeRegistries.BLOCKS.getValue(buf.readResourceLocation());

        int count = buf.readInt();
        StateDefinition<Block, BlockState> stateDefinition = block.getStateDefinition();

        properties = new HashSet<>();

        for (int i = 0; i < count; i++) {
            properties.add(stateDefinition.getProperty(buf.readUtf()));
        }
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        super.encode(buf);
        buf.writeResourceLocation(ForgeRegistries.BLOCKS.getKey(block));
        buf.writeInt(properties.size());
        properties.forEach((property) -> buf.writeUtf(property.getName()));
    }
}

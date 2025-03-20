package com.yanny.ali.plugin.function;

import com.yanny.ali.api.IContext;
import com.yanny.ali.mixin.MixinSetContainerLootTable;
import com.yanny.ali.plugin.FunctionTooltipUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

import java.util.List;
import java.util.Objects;

public class SetLootTableAliFunction extends LootConditionalAliFunction {
    public final ResourceLocation name;
    public final long seed;
    public final BlockEntityType<?> blockEntityType;

    public SetLootTableAliFunction(IContext context, LootItemFunction function) {
        super(context, function);
        name = ((MixinSetContainerLootTable) function).getName();
        seed = ((MixinSetContainerLootTable) function).getSeed();
        blockEntityType = ((MixinSetContainerLootTable) function).getType();
    }

    public SetLootTableAliFunction(IContext context, FriendlyByteBuf buf) {
        super(context, buf);
        name = buf.readResourceLocation();
        seed = buf.readLong();
        blockEntityType = BuiltInRegistries.BLOCK_ENTITY_TYPE.get(buf.readResourceLocation());
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        super.encode(context, buf);
        buf.writeResourceLocation(name);
        buf.writeLong(seed);
        buf.writeResourceLocation(Objects.requireNonNull(BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(blockEntityType)));
    }

    @Override
    public List<Component> getTooltip(int pad) {
        return FunctionTooltipUtils.getSetLootTableTooltip(pad, name, seed, blockEntityType);
    }
}

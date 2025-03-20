package com.yanny.ali.plugin.function;

import com.yanny.ali.api.IContext;
import com.yanny.ali.mixin.MixinSetContainerContents;
import com.yanny.ali.plugin.FunctionTooltipUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

import java.util.List;
import java.util.Objects;

public class SetContentsAliFunction extends LootConditionalAliFunction {
    public final BlockEntityType<?> blockEntityType;

    public SetContentsAliFunction(IContext context, LootItemFunction function) {
        super(context, function);
        blockEntityType = ((MixinSetContainerContents) function).getType();
    }

    public SetContentsAliFunction(IContext context, FriendlyByteBuf buf) {
        super(context, buf);
        blockEntityType = BuiltInRegistries.BLOCK_ENTITY_TYPE.get(buf.readResourceLocation());
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        super.encode(context, buf);
        buf.writeResourceLocation(Objects.requireNonNull(BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(blockEntityType)));
    }

    @Override
    public List<Component> getTooltip(int pad) {
        return FunctionTooltipUtils.getSetContentsTooltip(pad, blockEntityType);
    }
}

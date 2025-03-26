package com.yanny.ali.plugin.function;

import com.mojang.serialization.JsonOps;
import com.yanny.ali.api.IContext;
import com.yanny.ali.mixin.MixinSetContainerContents;
import com.yanny.ali.plugin.FunctionTooltipUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.storage.loot.ContainerComponentManipulator;
import net.minecraft.world.level.storage.loot.ContainerComponentManipulators;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

import java.util.List;

public class SetContentsAliFunction extends LootConditionalAliFunction {
    public final ContainerComponentManipulator<?> component;

    public SetContentsAliFunction(IContext context, LootItemFunction function) {
        super(context, function);
        component = ((MixinSetContainerContents) function).getComponent();
    }

    public SetContentsAliFunction(IContext context, FriendlyByteBuf buf) {
        super(context, buf);
        component = ContainerComponentManipulators.CODEC.decode(JsonOps.INSTANCE, buf.readJsonWithCodec(ExtraCodecs.JSON)).getOrThrow().getFirst();
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        super.encode(context, buf);
        buf.writeJsonWithCodec(ExtraCodecs.JSON, ContainerComponentManipulators.CODEC.encodeStart(JsonOps.INSTANCE, component).getOrThrow());
    }

    @Override
    public List<Component> getTooltip(int pad) {
        return FunctionTooltipUtils.getSetContentsTooltip(pad, component);
    }
}

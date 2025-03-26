package com.yanny.ali.plugin.function;

import com.mojang.serialization.JsonOps;
import com.yanny.ali.api.IContext;
import com.yanny.ali.mixin.MixinSetCustomDataFunction;
import com.yanny.ali.plugin.FunctionTooltipUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

import java.util.List;

public class SetCustomDataAliFunction extends LootConditionalAliFunction {
    public final CompoundTag tag;

    public SetCustomDataAliFunction(IContext context, LootItemFunction function) {
        super(context, function);
        tag = ((MixinSetCustomDataFunction) function).getTag();
    }

    public SetCustomDataAliFunction(IContext context, FriendlyByteBuf buf) {
        super(context, buf);
        tag = CompoundTag.CODEC.decode(JsonOps.INSTANCE, buf.readJsonWithCodec(ExtraCodecs.JSON)).getOrThrow().getFirst();
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        super.encode(context, buf);
        buf.writeJsonWithCodec(ExtraCodecs.JSON, CompoundTag.CODEC.encodeStart(JsonOps.INSTANCE, tag).getOrThrow());
    }

    @Override
    public List<Component> getTooltip(int pad) {
        return FunctionTooltipUtils.getSetCustomDataTooltip(pad, tag);
    }
}

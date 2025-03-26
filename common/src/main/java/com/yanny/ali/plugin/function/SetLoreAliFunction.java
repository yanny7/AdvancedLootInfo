package com.yanny.ali.plugin.function;

import com.mojang.serialization.JavaOps;
import com.mojang.serialization.JsonOps;
import com.yanny.ali.api.IContext;
import com.yanny.ali.mixin.MixinSetLoreFunction;
import com.yanny.ali.plugin.FunctionTooltipUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.ListOperation;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class SetLoreAliFunction extends LootConditionalAliFunction {
    public final ListOperation mode;
    public final List<Component> lore;
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public final Optional<LootContext.EntityTarget> resolutionContext;

    public SetLoreAliFunction(IContext context, LootItemFunction function) {
        super(context, function);
        mode = ((MixinSetLoreFunction) function).getMode();
        lore = ((MixinSetLoreFunction) function).getLore();
        resolutionContext = ((MixinSetLoreFunction) function).getResolutionContext();
    }

    public SetLoreAliFunction(IContext context, FriendlyByteBuf buf) {
        super(context, buf);
        mode = ListOperation.codec(255).compressedDecode(JavaOps.INSTANCE, buf.readJsonWithCodec(ExtraCodecs.JSON)).getOrThrow();
        lore = new LinkedList<>();

        int count = buf.readInt();

        for (int i = 0; i < count; i++) {
            lore.add(ComponentSerialization.CODEC.decode(JsonOps.INSTANCE, buf.readJsonWithCodec(ExtraCodecs.JSON)).getOrThrow().getFirst());
        }

        Optional<String> target = buf.readOptional(FriendlyByteBuf::readUtf);
        resolutionContext = target.map(LootContext.EntityTarget::getByName);
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        super.encode(context, buf);
        buf.writeJsonWithCodec(ExtraCodecs.JSON, ListOperation.codec(255).encode(mode, JsonOps.INSTANCE, JsonOps.INSTANCE.mapBuilder()).build(JsonOps.INSTANCE.empty()).getOrThrow());
        buf.writeInt(lore.size());
        lore.forEach((l) -> buf.writeJsonWithCodec(ExtraCodecs.JSON, ComponentSerialization.CODEC.encodeStart(JsonOps.INSTANCE, l).getOrThrow()));
        buf.writeOptional(resolutionContext.map(Enum::name), FriendlyByteBuf::writeUtf);
    }

    @Override
    public List<Component> getTooltip(int pad) {
        return FunctionTooltipUtils.getSetLoreTooltip(pad, mode, lore, resolutionContext);
    }
}

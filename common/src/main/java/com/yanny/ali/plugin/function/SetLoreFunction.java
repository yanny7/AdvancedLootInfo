package com.yanny.ali.plugin.function;

import com.mojang.serialization.JavaOps;
import com.mojang.serialization.JsonOps;
import com.yanny.ali.api.IContext;
import com.yanny.ali.mixin.MixinSetLoreFunction;
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

import static com.yanny.ali.plugin.TooltipUtils.*;

public class SetLoreFunction extends LootConditionalFunction {
    public final ListOperation.Type replace;
    public final List<Component> lore;
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public final Optional<LootContext.EntityTarget> resolutionContext;

    public SetLoreFunction(IContext context, LootItemFunction function) {
        super(context, function);
        replace = ((MixinSetLoreFunction) function).getMode().mode();
        lore = ((MixinSetLoreFunction) function).getLore();
        resolutionContext = ((MixinSetLoreFunction) function).getResolutionContext();
    }

    public SetLoreFunction(IContext context, FriendlyByteBuf buf) {
        super(context, buf);
        replace = ListOperation.Type.CODEC.decode(JavaOps.INSTANCE, buf.readJsonWithCodec(ExtraCodecs.JSON)).getOrThrow().getFirst();
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
        buf.writeJsonWithCodec(ExtraCodecs.JSON, ListOperation.Type.CODEC.encodeStart(JsonOps.INSTANCE, replace).getOrThrow());
        buf.writeInt(lore.size());
        lore.forEach((l) -> buf.writeJsonWithCodec(ExtraCodecs.JSON, ComponentSerialization.CODEC.encodeStart(JsonOps.INSTANCE, l).getOrThrow()));
        buf.writeOptional(resolutionContext.map(Enum::name), FriendlyByteBuf::writeUtf);
    }

    @Override
    public List<Component> getTooltip(int pad) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.set_lore")));
        components.add(pad(pad + 1, translatable("ali.property.function.set_lore.replace", replace.getSerializedName())));
        resolutionContext.ifPresent((c) -> components.add(pad(pad + 1, translatable("ali.property.function.set_lore.resolution_context", value(translatableType("ali.enum.target", c))))));
        components.add(pad(pad + 1, translatable("ali.property.function.set_lore.lore")));
        lore.forEach((l) -> components.add(pad(pad + 2, l)));

        return components;
    }
}

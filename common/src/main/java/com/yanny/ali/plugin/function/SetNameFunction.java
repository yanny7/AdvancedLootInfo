package com.yanny.ali.plugin.function;

import com.google.gson.JsonElement;
import com.yanny.ali.api.IContext;
import com.yanny.ali.mixin.MixinSetNameFunction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static com.yanny.ali.plugin.TooltipUtils.*;

public class SetNameFunction extends LootConditionalFunction {
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public final Optional<Component> name;
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public final Optional<LootContext.EntityTarget> resolutionContext;

    public SetNameFunction(IContext context, LootItemFunction function) {
        super(context, function);
        name = ((MixinSetNameFunction) function).getName();
        resolutionContext = ((MixinSetNameFunction) function).getResolutionContext();
    }

    public SetNameFunction(IContext context, FriendlyByteBuf buf) {
        super(context, buf);
        Optional<JsonElement> jsonElement = buf.readOptional((a) -> a.readJsonWithCodec(ExtraCodecs.JSON));
        name = jsonElement.map(Component.Serializer::fromJson);
        Optional<String> target = buf.readOptional(FriendlyByteBuf::readUtf);
        resolutionContext = target.map(LootContext.EntityTarget::getByName);
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        super.encode(context, buf);
        buf.writeOptional(name.map(Component.Serializer::toJson), FriendlyByteBuf::writeUtf);
        buf.writeOptional(resolutionContext.map(Enum::name), FriendlyByteBuf::writeUtf);
    }

    @Override
    public List<Component> getTooltip(int pad) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.set_name")));
        name.ifPresent((n) -> components.add(pad(pad + 1, translatable("ali.property.function.set_name.name", n))));
        resolutionContext.ifPresent((c) -> components.add(pad(pad + 1, translatable("ali.property.function.set_name.resolution_context", translatableType("ali.enum.target", c)))));

        return components;
    }
}

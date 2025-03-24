package com.yanny.ali.plugin.function;

import com.google.gson.JsonElement;
import com.yanny.ali.api.IContext;
import com.yanny.ali.mixin.MixinSetNameFunction;
import com.yanny.ali.plugin.FunctionTooltipUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

import java.util.List;
import java.util.Optional;

public class SetNameAliFunction extends LootConditionalAliFunction {
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public final Optional<Component> name;
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public final Optional<LootContext.EntityTarget> resolutionContext;

    public SetNameAliFunction(IContext context, LootItemFunction function) {
        super(context, function);
        name = ((MixinSetNameFunction) function).getName();
        resolutionContext = ((MixinSetNameFunction) function).getResolutionContext();
    }

    public SetNameAliFunction(IContext context, FriendlyByteBuf buf) {
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
        return FunctionTooltipUtils.getSetNameTooltip(pad, name, resolutionContext);
    }
}

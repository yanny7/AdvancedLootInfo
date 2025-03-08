package com.yanny.ali.plugin.function;

import com.yanny.ali.api.IContext;
import com.yanny.ali.mixin.MixinSetLoreFunction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static com.yanny.ali.plugin.TooltipUtils.*;

public class SetLoreFunction extends LootConditionalFunction {
    public final boolean replace;
    public final List<Component> lore;
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public final Optional<LootContext.EntityTarget> resolutionContext;

    public SetLoreFunction(IContext context, LootItemFunction function) {
        super(context, function);
        replace = ((MixinSetLoreFunction) function).getReplace();
        lore = ((MixinSetLoreFunction) function).getLore();
        resolutionContext = ((MixinSetLoreFunction) function).getResolutionContext();
    }

    public SetLoreFunction(IContext context, FriendlyByteBuf buf) {
        super(context, buf);
        replace = buf.readBoolean();
        lore = new LinkedList<>();

        int count = buf.readInt();

        for (int i = 0; i < count; i++) {
            lore.add(Component.Serializer.fromJson(buf.readJsonWithCodec(ExtraCodecs.JSON)));
        }

        Optional<String> target = buf.readOptional(FriendlyByteBuf::readUtf);
        resolutionContext = target.map(LootContext.EntityTarget::getByName);
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        super.encode(context, buf);
        buf.writeBoolean(replace);
        buf.writeInt(lore.size());
        lore.forEach((l) -> buf.writeJsonWithCodec(ExtraCodecs.JSON, Component.Serializer.toJsonTree(l)));
        buf.writeOptional(resolutionContext.map(Enum::name), FriendlyByteBuf::writeUtf);
    }

    @Override
    public List<Component> getTooltip(int pad) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.set_lore")));
        components.add(pad(pad + 1, translatable("ali.property.function.set_lore.replace", replace)));
        resolutionContext.ifPresent((c) -> components.add(pad(pad + 1, translatable("ali.property.function.set_lore.resolution_context", value(translatableType("ali.enum.target", c))))));
        components.add(pad(pad + 1, translatable("ali.property.function.set_lore.lore")));
        lore.forEach((l) -> components.add(pad(pad + 2, l)));

        return components;
    }
}

package com.yanny.advanced_loot_info.plugin.function;

import com.yanny.advanced_loot_info.api.IContext;
import com.yanny.advanced_loot_info.mixin.MixinSetNameFunction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static com.yanny.advanced_loot_info.compatibility.EmiUtils.*;

public class SetNameFunction extends LootConditionalFunction {
    public final Component name;
    @Nullable
    public final LootContext.EntityTarget resolutionContext;

    public SetNameFunction(IContext context, LootItemFunction function) {
        super(context, function);
        name = ((MixinSetNameFunction) function).getName();
        resolutionContext = ((MixinSetNameFunction) function).getResolutionContext();
    }

    public SetNameFunction(IContext context, FriendlyByteBuf buf) {
        super(context, buf);
        name = Component.Serializer.fromJson(buf.readJsonWithCodec(ExtraCodecs.JSON));
        String target = buf.readOptional(FriendlyByteBuf::readUtf).orElse(null);
        resolutionContext = target != null ? LootContext.EntityTarget.getByName(target) : null;
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        super.encode(context, buf);
        buf.writeJsonWithCodec(ExtraCodecs.JSON, Component.Serializer.toJsonTree(name));
        buf.writeOptional(Optional.ofNullable(resolutionContext != null ? resolutionContext.getName() : null), FriendlyByteBuf::writeUtf);
    }

    @Override
    public List<Component> getTooltip(int pad) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("emi.type.advanced_loot_info.function.set_name")));
        components.add(pad(pad + 1, translatable("emi.property.function.set_name.name", name)));

        if (resolutionContext != null) {
            components.add(pad(pad + 1, translatable("emi.property.function.set_name.resolution_context", translatableType("emi.enum.target", resolutionContext))));
        }

        return components;
    }
}

package com.yanny.ali.plugin.function;

import com.yanny.ali.api.IContext;
import com.yanny.ali.mixin.MixinSetLoreFunction;
import com.yanny.ali.plugin.FunctionTooltipUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class SetLoreAliFunction extends LootConditionalAliFunction {
    public final boolean replace;
    public final List<Component> lore;
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public final Optional<LootContext.EntityTarget> resolutionContext;

    public SetLoreAliFunction(IContext context, LootItemFunction function) {
        super(context, function);
        replace = ((MixinSetLoreFunction) function).getReplace();
        lore = ((MixinSetLoreFunction) function).getLore();
        resolutionContext = ((MixinSetLoreFunction) function).getResolutionContext();
    }

    public SetLoreAliFunction(IContext context, FriendlyByteBuf buf) {
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
        return FunctionTooltipUtils.getSetLoreTooltip(pad, replace, lore, resolutionContext);
    }
}

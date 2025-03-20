package com.yanny.ali.plugin.function;

import com.yanny.ali.api.IContext;
import com.yanny.ali.mixin.MixinLootContext;
import com.yanny.ali.mixin.MixinSetNameFunction;
import com.yanny.ali.plugin.FunctionTooltipUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public class SetNameAliFunction extends LootConditionalAliFunction {
    public final Component name;
    @Nullable
    public final LootContext.EntityTarget resolutionContext;

    public SetNameAliFunction(IContext context, LootItemFunction function) {
        super(context, function);
        name = ((MixinSetNameFunction) function).getName();
        resolutionContext = ((MixinSetNameFunction) function).getResolutionContext();
    }

    public SetNameAliFunction(IContext context, FriendlyByteBuf buf) {
        super(context, buf);
        name = Component.Serializer.fromJson(buf.readJsonWithCodec(ExtraCodecs.JSON));
        String target = buf.readOptional(FriendlyByteBuf::readUtf).orElse(null);
        resolutionContext = target != null ? LootContext.EntityTarget.getByName(target) : null;
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        super.encode(context, buf);
        buf.writeJsonWithCodec(ExtraCodecs.JSON, Component.Serializer.toJsonTree(name));
        buf.writeOptional(Optional.ofNullable(resolutionContext != null ? ((MixinLootContext.EntityTarget) ((Object) resolutionContext)).getName() : null), FriendlyByteBuf::writeUtf);
    }

    @Override
    public List<Component> getTooltip(int pad) {
        return FunctionTooltipUtils.getSetNameTooltip(pad, name, resolutionContext);
    }
}

package com.yanny.ali.plugin.function;

import com.mojang.serialization.JsonOps;
import com.yanny.ali.api.IContext;
import com.yanny.ali.mixin.MixinSetBannerPatternFunction;
import com.yanny.ali.plugin.FunctionTooltipUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

import java.util.List;

public class SetBannerPatternAliFunction extends LootConditionalAliFunction {
    public final boolean append;
    public final BannerPatternLayers patterns;

    public SetBannerPatternAliFunction(IContext context, LootItemFunction function) {
        super(context, function);
        append = ((MixinSetBannerPatternFunction) function).getAppend();
        patterns = ((MixinSetBannerPatternFunction) function).getPatterns();
    }

    public SetBannerPatternAliFunction(IContext context, FriendlyByteBuf buf) {
        super(context, buf);
        append = buf.readBoolean();
        patterns = BannerPatternLayers.CODEC.decode(JsonOps.INSTANCE, buf.readJsonWithCodec(ExtraCodecs.JSON)).getOrThrow().getFirst();
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        super.encode(context, buf);
        buf.writeBoolean(append);
        buf.writeJsonWithCodec(ExtraCodecs.JSON, BannerPatternLayers.CODEC.encodeStart(JsonOps.INSTANCE, patterns).getOrThrow());
    }

    @Override
    public List<Component> getTooltip(int pad) {
        return FunctionTooltipUtils.getSetBannerPatternTooltip(pad, append, patterns);
    }
}

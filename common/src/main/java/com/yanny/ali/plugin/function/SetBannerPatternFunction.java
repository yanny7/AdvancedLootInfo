package com.yanny.ali.plugin.function;

import com.mojang.serialization.JsonOps;
import com.yanny.ali.api.IContext;
import com.yanny.ali.mixin.MixinSetBannerPatternFunction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

import java.util.LinkedList;
import java.util.List;

import static com.yanny.ali.plugin.TooltipUtils.*;

public class SetBannerPatternFunction extends LootConditionalFunction {
    public final boolean append;
    public final BannerPatternLayers patterns;

    public SetBannerPatternFunction(IContext context, LootItemFunction function) {
        super(context, function);
        append = ((MixinSetBannerPatternFunction) function).getAppend();
        patterns = ((MixinSetBannerPatternFunction) function).getPatterns();
    }

    public SetBannerPatternFunction(IContext context, FriendlyByteBuf buf) {
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
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.set_banner_pattern")));
        components.add(pad(pad + 1, translatable("ali.property.function.set_banner_pattern.append", append)));

        if (!patterns.layers().isEmpty()) {
            components.add(pad(pad + 1, translatable("ali.property.function.set_banner_pattern.patterns")));

            patterns.layers().forEach((layer) -> {
                components.add(pad(pad + 2, value(translatable(layer.pattern().value().translationKey()))));
                components.add(pad(pad + 3, translatable("ali.property.function.set_banner_pattern.color", layer.color().getName())));
            });
        }

        return components;
    }
}

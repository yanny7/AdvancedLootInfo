package com.yanny.ali.plugin.function;

import com.mojang.serialization.JsonOps;
import com.yanny.ali.api.IContext;
import com.yanny.ali.mixin.MixinSetFireworkExplosionFunction;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static com.yanny.ali.plugin.TooltipUtils.pad;
import static com.yanny.ali.plugin.TooltipUtils.translatable;

public class SetFireworkExplosionFunction extends LootConditionalFunction {
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public final Optional<FireworkExplosion.Shape> shape;
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public final Optional<IntList> colors;
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public final Optional<IntList> fadeColors;
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public final Optional<Boolean> trail;
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public final Optional<Boolean> twinkle;

    public SetFireworkExplosionFunction(IContext context, LootItemFunction function) {
        super(context, function);
        shape = ((MixinSetFireworkExplosionFunction) function).getShape();
        colors = ((MixinSetFireworkExplosionFunction) function).getColors();
        fadeColors = ((MixinSetFireworkExplosionFunction) function).getFadeColors();
        trail = ((MixinSetFireworkExplosionFunction) function).getTrail();
        twinkle = ((MixinSetFireworkExplosionFunction) function).getTwinkle();
    }

    public SetFireworkExplosionFunction(IContext context, FriendlyByteBuf buf) {
        super(context, buf);
        shape = buf.readOptional((b) -> FireworkExplosion.Shape.CODEC.decode(JsonOps.INSTANCE, b.readJsonWithCodec(ExtraCodecs.JSON)).getOrThrow().getFirst());
        colors = buf.readOptional((b) -> FireworkExplosion.COLOR_LIST_CODEC.decode(JsonOps.INSTANCE, b.readJsonWithCodec(ExtraCodecs.JSON)).getOrThrow().getFirst());
        fadeColors = buf.readOptional((b) -> FireworkExplosion.COLOR_LIST_CODEC.decode(JsonOps.INSTANCE, b.readJsonWithCodec(ExtraCodecs.JSON)).getOrThrow().getFirst());
        trail = buf.readOptional(FriendlyByteBuf::readBoolean);
        twinkle = buf.readOptional(FriendlyByteBuf::readBoolean);
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        super.encode(context, buf);
        buf.writeOptional(shape, (b, s) -> b.writeJsonWithCodec(ExtraCodecs.JSON, FireworkExplosion.Shape.CODEC.encodeStart(JsonOps.INSTANCE, s).getOrThrow()));
        buf.writeOptional(colors, (b, s) -> b.writeJsonWithCodec(ExtraCodecs.JSON, FireworkExplosion.COLOR_LIST_CODEC.encodeStart(JsonOps.INSTANCE, s).getOrThrow()));
        buf.writeOptional(fadeColors, (b, s) -> b.writeJsonWithCodec(ExtraCodecs.JSON, FireworkExplosion.COLOR_LIST_CODEC.encodeStart(JsonOps.INSTANCE, s).getOrThrow()));
        buf.writeOptional(trail, FriendlyByteBuf::writeBoolean);
        buf.writeOptional(twinkle, FriendlyByteBuf::writeBoolean);
    }

    @Override
    public List<Component> getTooltip(int pad) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.set_firework_explosion")));
        shape.ifPresent((s) -> components.add(pad(pad + 1, translatable("ali.property.function.set_firework_explosion.shape", s.getSerializedName()))));
        colors.ifPresent((c) -> components.add(pad(pad + 1, translatable("ali.property.function.set_firework_explosion.colors", c))));
        fadeColors.ifPresent((c) -> components.add(pad(pad + 1, translatable("ali.property.function.set_firework_explosion.fade_colors", c))));
        trail.ifPresent((t) -> components.add(pad(pad + 1, translatable("ali.property.function.set_firework_explosion.has_trails", t))));
        twinkle.ifPresent((t) -> components.add(pad(pad + 1, translatable("ali.property.function.set_firework_explosion.has_twinkle", t))));

        return components;
    }
}

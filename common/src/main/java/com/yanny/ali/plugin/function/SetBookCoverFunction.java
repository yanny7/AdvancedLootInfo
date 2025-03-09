package com.yanny.ali.plugin.function;

import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.yanny.ali.api.IContext;
import com.yanny.ali.mixin.MixinSetBookCoverFunction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.network.Filterable;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static com.yanny.ali.plugin.TooltipUtils.pad;
import static com.yanny.ali.plugin.TooltipUtils.translatable;

public class SetBookCoverFunction extends LootConditionalFunction {
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public final Optional<String> author;
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public final Optional<Filterable<String>> title;
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public final Optional<Integer> generation;

    public SetBookCoverFunction(IContext context, LootItemFunction function) {
        super(context, function);
        author = ((MixinSetBookCoverFunction) function).getAuthor();
        title = ((MixinSetBookCoverFunction) function).getTitle();
        generation = ((MixinSetBookCoverFunction) function).getGeneration();
    }

    public SetBookCoverFunction(IContext context, FriendlyByteBuf buf) {
        super(context, buf);
        author = buf.readOptional(FriendlyByteBuf::readUtf);
        title = buf.readOptional((b) -> Filterable.codec(Codec.string(0, 32)).decode(JsonOps.INSTANCE, b.readJsonWithCodec(ExtraCodecs.JSON)).getOrThrow().getFirst());
        generation = buf.readOptional(FriendlyByteBuf::readInt);
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        super.encode(context, buf);
        buf.writeOptional(author, FriendlyByteBuf::writeUtf);
        buf.writeOptional(title, (b, s) -> b.writeJsonWithCodec(ExtraCodecs.JSON, Filterable.codec(Codec.string(0, 32)).encodeStart(JsonOps.INSTANCE, s).getOrThrow()));
        buf.writeOptional(generation, FriendlyByteBuf::writeInt);
    }

    @Override
    public List<Component> getTooltip(int pad) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.set_book_cover")));
        author.ifPresent((a) -> components.add(pad(pad + 1, translatable("ali.property.function.set_book_cover.author", a))));
        title.ifPresent((t) -> components.add(pad(pad + 1, translatable("ali.property.function.set_book_cover.title", t.raw()))));
        title.map(Filterable::filtered).ifPresent((f) -> components.add(pad(pad + 1, translatable("ali.property.function.set_book_cover.title.filtered", f))));
        generation.ifPresent((g) -> components.add(pad(pad + 1, translatable("ali.property.function.set_book_cover.generation", g))));

        return components;
    }
}

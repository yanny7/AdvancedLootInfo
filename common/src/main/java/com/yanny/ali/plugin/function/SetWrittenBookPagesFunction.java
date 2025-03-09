package com.yanny.ali.plugin.function;

import com.mojang.serialization.JsonOps;
import com.yanny.ali.api.IContext;
import com.yanny.ali.mixin.MixinSetWrittenBookPagesFunction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.server.network.Filterable;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

import java.util.LinkedList;
import java.util.List;

import static com.yanny.ali.plugin.TooltipUtils.pad;
import static com.yanny.ali.plugin.TooltipUtils.translatable;

public class SetWrittenBookPagesFunction extends LootConditionalFunction {
    public final List<Filterable<Component>> pages;
    public final String operation;

    public SetWrittenBookPagesFunction(IContext context, LootItemFunction function) {
        super(context, function);
        pages = ((MixinSetWrittenBookPagesFunction) function).getPages();
        operation = ((MixinSetWrittenBookPagesFunction) function).getPageOperation().mode().getSerializedName();
    }

    public SetWrittenBookPagesFunction(IContext context, FriendlyByteBuf buf) {
        super(context, buf);
        int count = buf.readInt();

        pages = new LinkedList<>();

        for (int i = 0; i < count; i++) {
            pages.add(Filterable.codec(ComponentSerialization.CODEC).decode(JsonOps.INSTANCE, buf.readJsonWithCodec(ExtraCodecs.JSON)).getOrThrow().getFirst());
        }

        operation = buf.readUtf();
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        super.encode(context, buf);
        buf.writeInt(pages.size());
        pages.forEach((p) -> buf.writeJsonWithCodec(ExtraCodecs.JSON, Filterable.codec(ComponentSerialization.CODEC).encodeStart(JsonOps.INSTANCE, p).getOrThrow()));
        buf.writeUtf(operation);
    }

    @Override
    public List<Component> getTooltip(int pad) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.set_written_book_pages")));

        if (!pages.isEmpty()) {
            pages.forEach((page) -> {
                components.add(pad(pad + 1, translatable("ali.property.function.set_written_book_pages.pages")));
                components.add(pad(pad + 2, translatable("ali.property.function.set_written_book_pages.pages.page", page.raw())));
                page.filtered().ifPresent((f) -> components.add(pad(pad + 3, translatable("ali.property.function.set_written_book_pages.pages.page.filtered", f))));
            });
        }

        components.add(pad(pad + 1, translatable("ali.property.function.set_written_book_pages.page_operation", operation)));

        return components;
    }
}

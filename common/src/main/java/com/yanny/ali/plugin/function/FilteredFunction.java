package com.yanny.ali.plugin.function;

import com.mojang.serialization.JavaOps;
import com.mojang.serialization.JsonOps;
import com.yanny.ali.api.IContext;
import com.yanny.ali.api.ILootFunction;
import com.yanny.ali.mixin.MixinFilteredFunction;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

import java.util.LinkedList;
import java.util.List;

import static com.yanny.ali.plugin.TooltipUtils.*;

public class FilteredFunction extends LootConditionalFunction {
    public final ItemPredicate filter;
    public final ILootFunction modifier;

    public FilteredFunction(IContext context, LootItemFunction function) {
        super(context, function);
        filter = ((MixinFilteredFunction) function).getFilter();
        modifier = context.utils().convertFunction(context, ((MixinFilteredFunction) function).getModifier());
    }

    public FilteredFunction(IContext context, FriendlyByteBuf buf) {
        super(context, buf);
        filter = ItemPredicate.CODEC.decode(JavaOps.INSTANCE, buf.readJsonWithCodec(ExtraCodecs.JSON)).getOrThrow().getFirst();
        modifier = context.utils().decodeFunction(context, buf);
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        super.encode(context, buf);
        buf.writeJsonWithCodec(ExtraCodecs.JSON, ItemPredicate.CODEC.encodeStart(JsonOps.INSTANCE, filter).getOrThrow());
        context.utils().encodeFunction(context, buf, modifier);
    }

    @Override
    public List<Component> getTooltip(int pad) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.filtered")));
        addItemPredicate(components, pad + 1, translatable("ali.property.function.filtered.filter"), filter);
        components.add(pad(pad + 1, translatable("ali.property.function.filtered.modifier")));
        components.addAll(modifier.getTooltip(pad + 2));

        return components;
    }
}

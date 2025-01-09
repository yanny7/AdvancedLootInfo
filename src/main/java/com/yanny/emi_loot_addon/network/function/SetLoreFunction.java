package com.yanny.emi_loot_addon.network.function;

import com.yanny.emi_loot_addon.mixin.MixinSetLoreFunction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static com.yanny.emi_loot_addon.compatibility.EmiUtils.*;

public class SetLoreFunction extends LootConditionalFunction {
    public final boolean replace;
    public final List<Component> lore;
    @Nullable
    public final LootContext.EntityTarget resolutionContext;

    public SetLoreFunction(LootContext lootContext, LootItemFunction function) {
        super(lootContext, function);
        replace = ((MixinSetLoreFunction) function).getReplace();
        lore = ((MixinSetLoreFunction) function).getLore();
        resolutionContext = ((MixinSetLoreFunction) function).getResolutionContext();
    }

    public SetLoreFunction(FunctionType type, FriendlyByteBuf buf) {
        super(type, buf);
        replace = buf.readBoolean();
        lore = new LinkedList<>();

        int count = buf.readInt();

        for (int i = 0; i < count; i++) {
            lore.add(Component.Serializer.fromJson(buf.readJsonWithCodec(ExtraCodecs.JSON)));
        }

        String target = buf.readOptional(FriendlyByteBuf::readUtf).orElse(null);
        resolutionContext = target != null ? LootContext.EntityTarget.getByName(target) : null;
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        super.encode(buf);
        buf.writeBoolean(replace);
        buf.writeInt(lore.size());
        lore.forEach((l) -> buf.writeJsonWithCodec(ExtraCodecs.JSON, Component.Serializer.toJsonTree(l)));
        buf.writeOptional(Optional.ofNullable(resolutionContext != null ? resolutionContext.getName() : null), FriendlyByteBuf::writeUtf);
    }

    @Override
    public List<Component> getTooltip(int pad) {
        List<Component> components = super.getTooltip(pad);

        components.add(pad(pad + 1, translatable("emi.property.function.set_lore.replace", replace)));

        if (resolutionContext != null) {
            components.add(pad(pad + 1, translatable("emi.property.function.set_lore.resolution_context", value(translatableType("emi.enum.target", resolutionContext)))));
        }

        components.add(pad(pad + 1, translatable("emi.property.function.set_lore.lore")));
        lore.forEach((l) -> components.add(pad(pad + 2, l)));

        return components;
    }
}

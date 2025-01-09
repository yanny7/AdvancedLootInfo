package com.yanny.advanced_loot_info.network.function;

import com.yanny.advanced_loot_info.mixin.MixinFillPlayerHead;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

import java.util.List;

import static com.yanny.advanced_loot_info.compatibility.EmiUtils.pad;
import static com.yanny.advanced_loot_info.compatibility.EmiUtils.translatable;

public class FillPlayerHeadFunction extends LootConditionalFunction {
    public final LootContext.EntityTarget entityTarget;

    public FillPlayerHeadFunction(LootContext lootContext, LootItemFunction function) {
        super(lootContext, function);
        entityTarget = ((MixinFillPlayerHead) function).getEntityTarget();
    }

    public FillPlayerHeadFunction(FunctionType type, FriendlyByteBuf buf) {
        super(type, buf);
        entityTarget = LootContext.EntityTarget.getByName(buf.readUtf());
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        super.encode(buf);
        buf.writeUtf(entityTarget.getName());
    }

    @Override
    public List<Component> getTooltip(int pad) {
        List<Component> components = super.getTooltip(pad);

        components.add(pad(pad + 1, translatable("emi.property.function.fill_player_head.target", entityTarget)));

        return components;
    }
}

package com.yanny.ali.plugin.condition;

import com.yanny.ali.api.IContext;
import com.yanny.ali.api.ILootCondition;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.predicates.ConditionReference;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.LinkedList;
import java.util.List;

import static com.yanny.ali.plugin.TooltipUtils.pad;
import static com.yanny.ali.plugin.TooltipUtils.translatable;

public class ReferenceCondition implements ILootCondition {
    public final ResourceKey<LootItemCondition> name;

    public ReferenceCondition(IContext context, LootItemCondition condition) {
        name = ((ConditionReference) condition).name();
    }

    public ReferenceCondition(IContext context, FriendlyByteBuf buf) {
        name = buf.readResourceKey(Registries.PREDICATE);
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        buf.writeResourceKey(name);
    }

    @Override
    public List<Component> getTooltip(int pad) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.condition.reference", name.location())));

        return components;
    }
}

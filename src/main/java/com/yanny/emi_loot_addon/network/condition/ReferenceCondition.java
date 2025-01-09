package com.yanny.emi_loot_addon.network.condition;

import com.yanny.emi_loot_addon.mixin.MixinConditionReference;
import com.yanny.emi_loot_addon.network.LootCondition;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.LinkedList;
import java.util.List;

import static com.yanny.emi_loot_addon.compatibility.EmiUtils.pad;
import static com.yanny.emi_loot_addon.compatibility.EmiUtils.translatableType;

public class ReferenceCondition extends LootCondition {
    public final ResourceLocation name;

    public ReferenceCondition(LootContext lootContext, LootItemCondition condition) {
        super(ConditionType.of(condition.getType()));
        name = ((MixinConditionReference) condition).getName();
    }

    public ReferenceCondition(ConditionType type, FriendlyByteBuf buf) {
        super(type);
        name = buf.readResourceLocation();
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeResourceLocation(name);
    }

    @Override
    public List<Component> getTooltip(int pad) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatableType("emi.type.emi_loot_addon.condition", type, name)));

        return components;
    }
}

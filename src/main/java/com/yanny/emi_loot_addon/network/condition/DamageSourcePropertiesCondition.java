package com.yanny.emi_loot_addon.network.condition;

import com.yanny.emi_loot_addon.mixin.MixinDamageSourceCondition;
import com.yanny.emi_loot_addon.mixin.MixinDamageSourcePredicate;
import com.yanny.emi_loot_addon.mixin.MixinTagPredicate;
import com.yanny.emi_loot_addon.network.LootCondition;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DamageSourcePropertiesCondition extends LootCondition {
    public final List<Map.Entry<ResourceLocation, Boolean>> damageSources;

    public DamageSourcePropertiesCondition(LootContext lootContext, LootItemCondition condition) {
        super(ConditionType.of(condition.getType()));
        damageSources = ((MixinDamageSourcePredicate) ((MixinDamageSourceCondition) condition).getPredicate()).getTags().stream()
                .map((tag) -> Map.entry(((MixinTagPredicate<?>) tag).getTag().location(), ((MixinTagPredicate<?>) tag).getExpected())).collect(Collectors.toList());
    }

    public DamageSourcePropertiesCondition(ConditionType type, @NotNull FriendlyByteBuf buf) {
        super(type);
        damageSources = new LinkedList<>();

        int count = buf.readInt();

        for (int i = 0; i < count; i++) {
            damageSources.add(Map.entry(buf.readResourceLocation(), buf.readBoolean()));
        }
    }

    @Override
    public void encode(@NotNull FriendlyByteBuf buf) {
        buf.writeInt(damageSources.size());
        damageSources.forEach((d) -> {
            buf.writeResourceLocation(d.getKey());
            buf.writeBoolean(d.getValue());
        });
    }
}

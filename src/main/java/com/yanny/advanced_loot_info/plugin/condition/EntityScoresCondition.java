package com.yanny.advanced_loot_info.plugin.condition;

import com.yanny.advanced_loot_info.api.IContext;
import com.yanny.advanced_loot_info.api.ILootCondition;
import com.yanny.advanced_loot_info.api.RangeValue;
import com.yanny.advanced_loot_info.mixin.MixinEntityHasScoreCondition;
import com.yanny.advanced_loot_info.mixin.MixinIntRange;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.yanny.advanced_loot_info.compatibility.EmiUtils.*;

public class EntityScoresCondition implements ILootCondition {
    public final Map<String, Tuple<RangeValue, RangeValue>> scores;
    public final LootContext.EntityTarget target;

    public EntityScoresCondition(IContext context, LootItemCondition condition) {
        target = ((MixinEntityHasScoreCondition) condition).getEntityTarget();
        scores = ((MixinEntityHasScoreCondition) condition).getScores().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, (e) -> new Tuple<>(
                        context.utils().convertNumber(context, ((MixinIntRange)e.getValue()).getMin()),
                        context.utils().convertNumber(context, ((MixinIntRange)e.getValue()).getMax())
                )));
    }

    public EntityScoresCondition(IContext context, FriendlyByteBuf buf) {
        target = buf.readEnum(LootContext.EntityTarget.class);

        int size = buf.readInt();

        scores = new HashMap<>(size);

        for (int i = 0; i < size; i++) {
            scores.put(buf.readUtf(), new Tuple<>(new RangeValue(buf), new RangeValue(buf)));
        }
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        buf.writeEnum(target);
        buf.writeInt(scores.size());

        scores.forEach((s, r) -> {
            buf.writeUtf(s);
            r.getA().encode(buf);
            r.getB().encode(buf);
        });
    }

    @Override
    public List<Component> getTooltip(int pad) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("emi.type.advanced_loot_info.condition.entity_scores")));
        components.add(pad(pad + 1, translatable("emi.property.condition.predicate.target", value(translatableType("emi.enum.target", target)))));
        components.add(pad(pad + 1, translatable("emi.property.condition.scores.score")));
        scores.forEach((score, tuple) -> components.add(pad(pad + 2, keyValue(score, RangeValue.rangeToString(tuple.getA(), tuple.getB())))));

        return components;
    }
}

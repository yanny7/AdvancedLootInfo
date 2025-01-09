package com.yanny.emi_loot_addon.network.condition;

import com.yanny.emi_loot_addon.mixin.MixinEntityHasScoreCondition;
import com.yanny.emi_loot_addon.mixin.MixinIntRange;
import com.yanny.emi_loot_addon.network.LootCondition;
import com.yanny.emi_loot_addon.network.RangeValue;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.yanny.emi_loot_addon.compatibility.EmiUtils.*;

public class EntityScoresCondition extends LootCondition {
    public final Map<String, Tuple<RangeValue, RangeValue>> scores;
    public final LootContext.EntityTarget target;

    public EntityScoresCondition(LootContext lootContext, LootItemCondition condition) {
        super(ConditionType.of(condition.getType()));
        target = ((MixinEntityHasScoreCondition) condition).getEntityTarget();
        scores = ((MixinEntityHasScoreCondition) condition).getScores().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, (e) -> new Tuple<>(
                        RangeValue.of(lootContext, ((MixinIntRange)e.getValue()).getMin()),
                        RangeValue.of(lootContext, ((MixinIntRange)e.getValue()).getMax())
                )));
    }

    public EntityScoresCondition(ConditionType type, FriendlyByteBuf buf) {
        super(type);
        target = buf.readEnum(LootContext.EntityTarget.class);

        int size = buf.readInt();

        scores = new HashMap<>(size);

        for (int i = 0; i < size; i++) {
            scores.put(buf.readUtf(), new Tuple<>(new RangeValue(buf), new RangeValue(buf)));
        }
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
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

        List<Component> components = super.getTooltip(pad);

        components.add(pad(pad + 1, translatable("emi.property.condition.predicate.target", value(translatableType("emi.enum.target", target)))));
        components.add(pad(pad + 1, translatable("emi.property.condition.scores.score")));
        scores.forEach((score, tuple) -> components.add(pad(pad + 2, keyValue(score, RangeValue.rangeToString(tuple.getA(), tuple.getB())))));

        return components;
    }
}

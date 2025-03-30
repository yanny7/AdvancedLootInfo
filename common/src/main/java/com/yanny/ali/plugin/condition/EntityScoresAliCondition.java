package com.yanny.ali.plugin.condition;

import com.yanny.ali.api.IContext;
import com.yanny.ali.api.ILootCondition;
import com.yanny.ali.api.RangeValue;
import com.yanny.ali.mixin.MixinEntityHasScoreCondition;
import com.yanny.ali.mixin.MixinIntRange;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class EntityScoresAliCondition implements ILootCondition {
    public final Map<String, Tuple<RangeValue, RangeValue>> scores;
    public final LootContext.EntityTarget target;

    public EntityScoresAliCondition(IContext context, LootItemCondition condition) {
        target = ((MixinEntityHasScoreCondition) condition).getEntityTarget();
        scores = ((MixinEntityHasScoreCondition) condition).getScores().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, (e) -> new Tuple<>(
                        context.utils().convertNumber(context, ((MixinIntRange)e.getValue()).getMin()),
                        context.utils().convertNumber(context, ((MixinIntRange)e.getValue()).getMax())
                )));
    }

    public EntityScoresAliCondition(IContext context, FriendlyByteBuf buf) {
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
}

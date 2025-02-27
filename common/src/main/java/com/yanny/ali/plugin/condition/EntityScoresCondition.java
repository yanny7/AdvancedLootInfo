package com.yanny.ali.plugin.condition;

import com.yanny.ali.api.IContext;
import com.yanny.ali.api.ILootCondition;
import com.yanny.ali.api.RangeValue;
import com.yanny.ali.mixin.MixinIntRange;
import com.yanny.ali.plugin.TooltipUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.EntityHasScoreCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EntityScoresCondition implements ILootCondition {
    public final Map<String, Tuple<RangeValue, RangeValue>> scores;
    public final LootContext.EntityTarget target;

    public EntityScoresCondition(IContext context, LootItemCondition condition) {
        target = ((EntityHasScoreCondition) condition).entityTarget();
        scores = ((EntityHasScoreCondition) condition).scores().entrySet().stream()
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

        components.add(TooltipUtils.pad(pad, TooltipUtils.translatable("ali.type.condition.entity_scores")));
        components.add(TooltipUtils.pad(pad + 1, TooltipUtils.translatable("ali.property.condition.predicate.target", TooltipUtils.value(TooltipUtils.translatableType("ali.enum.target", target)))));
        components.add(TooltipUtils.pad(pad + 1, TooltipUtils.translatable("ali.property.condition.scores.score")));
        scores.forEach((score, tuple) -> components.add(TooltipUtils.pad(pad + 2, TooltipUtils.keyValue(score, RangeValue.rangeToString(tuple.getA(), tuple.getB())))));

        return components;
    }
}

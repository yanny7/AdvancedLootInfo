package com.yanny.ali.plugin.glm;

import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.List;

public record PageMatch(Match match, List<LootItemCondition> unexplained) {
    public static final PageMatch NO = new PageMatch(Match.NO, List.of());
}

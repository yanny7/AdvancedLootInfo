package com.yanny.alicompat.accessor;

import com.yanny.ali.plugin.common.NodeUtils;
import net.minecraft.world.level.storage.loot.entries.UniformContainerBase;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.List;

public abstract class SingletonContainer extends BaseAccessor<UniformContainerBase> {
    public final int weight;
    public final int quality;
    public final List<LootItemFunction> functions;
    public final List<LootItemCondition> conditions;

    public SingletonContainer(UniformContainerBase parent) {
        super(parent);
        weight = parent.weight;
        quality = parent.quality;
        functions = NodeUtils.unwrapFunctions(parent.modifier);
        conditions = NodeUtils.unwrapConditions(parent.condition);
    }
}

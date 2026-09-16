package com.yanny.alicompat.compat.artifacts;

import artifacts.neoforge.loot.RollLootTableModifier;
import com.yanny.ali.api.IOperation;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.plugin.glm.GlobalLootModifierUtils;
import com.yanny.ali.plugin.glm.IPageLootModifier;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.GlmNodeUtils;
import com.yanny.alicompat.accessor.IGlobalLootModifierAccessor;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class RollLootTableModifierAccessor extends BaseAccessor<RollLootTableModifier> implements IGlobalLootModifierAccessor {
    @FieldAccessor
    private ResourceKey<LootTable> lootTable;
    @FieldAccessor
    private boolean replace;
    @FieldAccessor
    protected LootItemCondition[] conditions;

    public RollLootTableModifierAccessor(RollLootTableModifier parent) {
        super(parent);
    }

    @Override
    public Optional<IPageLootModifier> getLootModifier(IServerUtils utils) {
        return Optional.of(GlobalLootModifierUtils.getLootModifier(utils, parent, Arrays.asList(this.conditions), (c) -> {
            List<IOperation> operations = new ArrayList<>();

            if (replace) {
                operations.add(new IOperation.RemoveOperation((itemStack) -> true, (src) -> GlmNodeUtils.keptNode(utils, c, src)));
            }

            operations.add(new IOperation.AddOperation((itemStack) -> true, GlmNodeUtils.referenceNode(utils, c, lootTable.location())));
            return operations;
        }));
    }
}

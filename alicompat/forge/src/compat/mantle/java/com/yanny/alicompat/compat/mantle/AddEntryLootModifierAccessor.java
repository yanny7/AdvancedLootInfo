package com.yanny.alicompat.compat.mantle;

import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.ILootModifier;
import com.yanny.ali.api.IOperation;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.plugin.glm.GlobalLootModifierUtils;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.IGlobalLootModifierAccessor;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;
import slimeknights.mantle.loot.AddEntryLootModifier;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class AddEntryLootModifierAccessor extends BaseAccessor<AddEntryLootModifier> implements IGlobalLootModifierAccessor {
    @FieldAccessor
    private LootPoolEntryContainer entry;

    @FieldAccessor
    private LootItemFunction[] functions;

    @FieldAccessor
    protected LootItemCondition[] conditions;

    public AddEntryLootModifierAccessor(AddEntryLootModifier parent) {
        super(parent);
    }

    public Optional<ILootModifier<?>> getLootModifier(IServerUtils utils) {
        List<LootItemCondition> conditionList = Arrays.asList(conditions);

        return GlobalLootModifierUtils.getLootModifier(utils, parent, conditionList,
                (c) -> Collections.singletonList(new IOperation.AddOperation((itemStack) -> true, getEntryNode(utils, c))));
    }

    @NotNull
    private IDataNode getEntryNode(IServerUtils utils, List<LootItemCondition> conditions) {
        int sumWeight = entry instanceof LootPoolSingletonContainer singleton ? singleton.weight : 1;

        return utils.getEntryFactory(utils, entry).create(utils, entry, 1, sumWeight, Arrays.asList(functions), conditions);
    }
}

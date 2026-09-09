package com.yanny.alicompat.compat.farmersdelight;

import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.ILootModifier;
import com.yanny.ali.api.IOperation;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.plugin.glm.GlobalLootModifierUtils;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.GlmNodeUtils;
import com.yanny.alicompat.accessor.IGlobalLootModifierAccessor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import vectorwing.farmersdelight.common.loot.modifier.AddLootTableModifier;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class AddLootTableModifierAccessor extends BaseAccessor<AddLootTableModifier> implements IGlobalLootModifierAccessor {
    @FieldAccessor
    private ResourceLocation lootTable;
    @FieldAccessor
    protected LootItemCondition[] conditions;

    public AddLootTableModifierAccessor(AddLootTableModifier parent) {
        super(parent);
    }

    public Optional<ILootModifier<?>> getLootModifier(IServerUtils utils) {
        List<LootItemCondition> conditionList = Arrays.asList(this.conditions);

        return GlobalLootModifierUtils.getLootModifier(utils, parent, conditionList, (c) -> {
            IDataNode node = GlmNodeUtils.referenceNode(utils, c, lootTable);
            return Collections.singletonList(new IOperation.AddOperation((itemStack) -> true, node));
        });
    }
}

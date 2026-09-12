package com.yanny.alicompat.compat.ironsspellbooks;

import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.ILootModifier;
import com.yanny.ali.api.IOperation;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.plugin.glm.GlobalLootModifierUtils;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.GlmNodeUtils;
import com.yanny.alicompat.accessor.IGlobalLootModifierAccessor;
import io.redspace.ironsspellbooks.loot.AppendLootModifier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class AppendLootModifierAccessor extends BaseAccessor<AppendLootModifier> implements IGlobalLootModifierAccessor {
    @FieldAccessor
    private String resourceLocationKey;
    @FieldAccessor
    protected LootItemCondition[] conditions;

    public AppendLootModifierAccessor(AppendLootModifier parent) {
        super(parent);
    }

    @Override
    public Optional<ILootModifier<?>> getLootModifier(IServerUtils utils) {
        List<LootItemCondition> conditionList = Arrays.asList(this.conditions);
        ResourceLocation lootTable = ResourceLocation.parse(resourceLocationKey);

        return GlobalLootModifierUtils.getLootModifier(utils, parent, conditionList, (c) -> {
            IDataNode node = GlmNodeUtils.referenceNode(utils, c, lootTable);

            return Collections.singletonList(new IOperation.AddOperation((itemStack) -> true, node));
        });
    }
}

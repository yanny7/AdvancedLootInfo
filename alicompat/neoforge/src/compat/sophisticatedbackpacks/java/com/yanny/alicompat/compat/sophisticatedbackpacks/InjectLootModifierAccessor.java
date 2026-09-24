package com.yanny.alicompat.compat.sophisticatedbackpacks;

import com.yanny.ali.api.IDataNode;
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
import net.p3pp3rf1y.sophisticatedbackpacks.data.BackpackLootModifierProvider;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

public class InjectLootModifierAccessor extends BaseAccessor<BackpackLootModifierProvider.InjectLootModifier> implements IGlobalLootModifierAccessor {
    @FieldAccessor
    private ResourceKey<LootTable> lootTable;
    @FieldAccessor
    protected LootItemCondition[] conditions;

    public InjectLootModifierAccessor(BackpackLootModifierProvider.InjectLootModifier parent) {
        super(parent);
    }

    @Override
    public Optional<IPageLootModifier> getLootModifier(IServerUtils utils) {
        return Optional.of(GlobalLootModifierUtils.getLootModifier(utils, parent, Arrays.asList(this.conditions), (c) -> {
            IDataNode node = GlmNodeUtils.referenceNode(utils, c, lootTable.identifier());

            return Collections.singletonList(new IOperation.AddOperation((itemStack) -> true, node));
        }));
    }
}

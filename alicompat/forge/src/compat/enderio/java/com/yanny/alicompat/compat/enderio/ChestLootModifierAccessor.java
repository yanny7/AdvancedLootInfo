package com.yanny.alicompat.compat.enderio;

import com.enderio.base.common.loot.ChestLootModifier;
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

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

public class ChestLootModifierAccessor extends BaseAccessor<ChestLootModifier> implements IGlobalLootModifierAccessor {
    @FieldAccessor
    private ResourceLocation lootTable;
    @FieldAccessor
    protected LootItemCondition[] conditions;

    public ChestLootModifierAccessor(ChestLootModifier parent) {
        super(parent);
    }

    @Override
    public Optional<ILootModifier<?>> getLootModifier(IServerUtils utils) {
        return GlobalLootModifierUtils.getLootModifier(utils, parent, Arrays.asList(this.conditions), (c) -> {
            IDataNode node = GlmNodeUtils.referenceNode(utils, c, lootTable);

            return Collections.singletonList(new IOperation.AddOperation((itemStack) -> true, node));
        });
    }
}

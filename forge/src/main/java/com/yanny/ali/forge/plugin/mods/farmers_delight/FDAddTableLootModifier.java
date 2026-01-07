package com.yanny.ali.forge.plugin.mods.farmers_delight;

import com.yanny.ali.api.*;
import com.yanny.ali.forge.plugin.GlobalLootModifier;
import com.yanny.ali.plugin.common.NodeUtils;
import com.yanny.ali.plugin.common.tooltip.ArrayTooltipNode;
import com.yanny.ali.plugin.common.tooltip.LiteralTooltipNode;
import com.yanny.ali.plugin.glm.GlobalLootModifierUtils;
import com.yanny.ali.plugin.glm.IGlobalLootModifierAccessor;
import com.yanny.ali.plugin.glm.ILootTableIdConditionPredicate;
import com.yanny.ali.plugin.mods.ClassAccessor;
import com.yanny.ali.plugin.mods.FieldAccessor;
import com.yanny.ali.plugin.server.GenericTooltipUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.LootModifier;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@ClassAccessor("vectorwing.farmersdelight.common.loot.modifier.FDAddTableLootModifier")
public class FDAddTableLootModifier extends GlobalLootModifier implements IGlobalLootModifierAccessor {
    @FieldAccessor
    private ResourceKey<LootTable> lootTable;

    public FDAddTableLootModifier(LootModifier parent) {
        super(parent);
    }

    public Optional<ILootModifier<?>> getLootModifier(IServerUtils utils, ILootTableIdConditionPredicate predicate) {
        List<LootItemCondition> conditionList = Arrays.asList(this.conditions);

        return GlobalLootModifierUtils.getLootModifier(conditionList, (c) -> {
            ITooltipNode tooltip = ArrayTooltipNode.array()
                    .add(LiteralTooltipNode.translatable("ali.enum.group_type.all"))
                    .add(GenericTooltipUtils.getConditionsTooltip(utils, c))
                    .build();
            IDataNode node = NodeUtils.getReferenceNode(utils, lootTable.location(), c, tooltip);
            return Collections.singletonList(new IOperation.AddOperation((itemStack) -> true, node));
        }, predicate);
    }
}

package com.yanny.ali.plugin.mods.farmers_delight_glm;

import com.yanny.ali.api.*;
import com.yanny.ali.plugin.GlobalLootModifier;
import com.yanny.ali.plugin.GlobalLootModifierUtils;
import com.yanny.ali.plugin.IForgeLootModifier;
import com.yanny.ali.plugin.common.nodes.ReferenceNode;
import com.yanny.ali.plugin.common.tooltip.ArrayTooltipNode;
import com.yanny.ali.plugin.common.tooltip.LiteralTooltipNode;
import com.yanny.ali.plugin.mods.ClassAccessor;
import com.yanny.ali.plugin.mods.FieldAccessor;
import com.yanny.ali.plugin.server.GenericTooltipUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.LootModifier;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@ClassAccessor("vectorwing.farmersdelight.common.loot.modifier.FDAddTableLootModifier")
public class FDAddTableLootModifier extends GlobalLootModifier implements IForgeLootModifier {
    @FieldAccessor
    private ResourceKey<LootTable> lootTable;

    public FDAddTableLootModifier(LootModifier parent) {
        super(parent);
    }

    public Optional<ILootModifier<?>> getLootModifier(IServerUtils utils) {
        List<LootItemCondition> conditionList = Arrays.asList(this.conditions);

        return GlobalLootModifierUtils.getLootModifier(conditionList, (c) -> {
            ITooltipNode tooltip = ArrayTooltipNode.array()
                    .add(LiteralTooltipNode.translatable("ali.enum.group_type.all"))
                    .add(GenericTooltipUtils.getConditionsTooltip(utils, c))
                    .build();
            IDataNode node = new ReferenceNode(utils, lootTable.location(), c, tooltip);
            return Collections.singletonList(new IOperation.AddOperation((itemStack) -> true, node));
        });
    }
}

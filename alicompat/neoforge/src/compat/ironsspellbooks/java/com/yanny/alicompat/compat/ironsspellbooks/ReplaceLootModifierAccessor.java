package com.yanny.alicompat.compat.ironsspellbooks;

import com.mojang.datafixers.util.Either;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.ILootModifier;
import com.yanny.ali.api.IOperation;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import com.yanny.ali.plugin.common.NodeUtils;
import com.yanny.ali.plugin.common.nodes.MissingNode;
import com.yanny.ali.plugin.common.nodes.ReferenceNode;
import com.yanny.ali.plugin.glm.GlobalLootModifierUtils;
import com.yanny.ali.plugin.server.EnchantedRanges;
import com.yanny.ali.plugin.server.GenericTooltipUtils;
import com.yanny.ali.plugin.server.TooltipUtils;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.IGlobalLootModifierAccessor;
import io.redspace.ironsspellbooks.loot.ReplaceLootModifier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class ReplaceLootModifierAccessor extends BaseAccessor<ReplaceLootModifier> implements IGlobalLootModifierAccessor {
    @FieldAccessor
    private String resourceLocationKey;
    @FieldAccessor
    private double chanceToReplace;
    @FieldAccessor
    protected LootItemCondition[] conditions;

    public ReplaceLootModifierAccessor(ReplaceLootModifier parent) {
        super(parent);
    }

    @Override
    public Optional<ILootModifier<?>> getLootModifier(IServerUtils utils) {
        List<LootItemCondition> conditionList = Arrays.asList(this.conditions);
        ResourceLocation lootTable = ResourceLocation.parse(resourceLocationKey);
        float chance = (float) chanceToReplace;

        return GlobalLootModifierUtils.getLootModifier(utils, parent, conditionList, (c) -> {
            TooltipNode tooltip = TooltipBuilder.array((b) -> {
                b.add(TooltipBuilder.keyOnly(Lang.Group.ALL));
                b.add(TooltipUtils.getChanceTooltip(new EnchantedRanges(chance * 100)));
                b.add(GenericTooltipUtils.getConditionsSectionTooltip(utils, c));
            }).build();
            LootTable table = utils.getLootTable(Either.left(lootTable));
            IDataNode child;

            if (table != null) {
                child = NodeUtils.getLootTableNode(Collections.emptyList(), utils, table, chance, Collections.emptyList(), c);
            } else {
                child = new MissingNode(utils.getValueTooltip(utils, lootTable).build(Lang.Value.LOOT_TABLE));
            }

            IDataNode node = new ReferenceNode(Collections.singletonList(child), chance, tooltip);

            return Collections.singletonList(new IOperation.AddOperation((itemStack) -> true, node));
        });
    }
}

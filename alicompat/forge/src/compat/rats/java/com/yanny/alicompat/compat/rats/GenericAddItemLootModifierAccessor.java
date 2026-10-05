package com.yanny.alicompat.compat.rats;

import com.github.alexthe666.rats.server.loot.GenericAddItemLootModifier;
import com.yanny.aci.api.NumberExpr;
import com.yanny.ali.api.IOperation;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.plugin.glm.GlobalLootModifierUtils;
import com.yanny.ali.plugin.glm.IPageLootModifier;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.GlmNodeUtils;
import com.yanny.alicompat.accessor.IGlobalLootModifierAccessor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class GenericAddItemLootModifierAccessor extends BaseAccessor<GenericAddItemLootModifier> implements IGlobalLootModifierAccessor {
    @FieldAccessor
    private Map<Item, Integer> items;
    @FieldAccessor
    private boolean makeNewPool;
    @FieldAccessor
    protected LootItemCondition[] conditions;

    public GenericAddItemLootModifierAccessor(GenericAddItemLootModifier parent) {
        super(parent);
    }

    @Override
    public Optional<IPageLootModifier> getLootModifier(IServerUtils utils) {
        float chance = makeNewPool && items.size() > 1 ? 1F / items.size() : 1F;

        return Optional.of(GlobalLootModifierUtils.getLootModifier(utils, parent, Arrays.asList(this.conditions), (page, c) -> {
            List<IOperation> operations = new ArrayList<>();

            if (makeNewPool) {
                operations.add(new IOperation.RemoveOperation((itemStack) -> true, (src) -> GlmNodeUtils.keptNode(utils, c, src)));
            }

            items.forEach((item, count) -> operations.add(new IOperation.AddOperation((itemStack) -> true,
                    GlmNodeUtils.addedNode(utils, c, item.getDefaultInstance(), chance, NumberExpr.constant(count)))));
            return operations;
        }));
    }
}

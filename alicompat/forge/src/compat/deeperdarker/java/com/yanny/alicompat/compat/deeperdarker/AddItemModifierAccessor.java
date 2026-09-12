package com.yanny.alicompat.compat.deeperdarker;

import com.kyanite.deeperdarker.content.loot.AddItemModifier;
import com.yanny.aci.api.RangeValue;
import com.yanny.ali.api.ILootModifier;
import com.yanny.ali.api.IOperation;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.plugin.glm.GlobalLootModifierUtils;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.GlmNodeUtils;
import com.yanny.alicompat.accessor.IGlobalLootModifierAccessor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class AddItemModifierAccessor extends BaseAccessor<AddItemModifier> implements IGlobalLootModifierAccessor {
    @FieldAccessor
    private Item item;

    @FieldAccessor
    private int min;

    @FieldAccessor
    private int max;

    @FieldAccessor
    protected LootItemCondition[] conditions;

    public AddItemModifierAccessor(AddItemModifier parent) {
        super(parent);
    }

    public Optional<ILootModifier<?>> getLootModifier(IServerUtils utils) {
        List<LootItemCondition> conditionList = Arrays.asList(conditions);

        return GlobalLootModifierUtils.getLootModifier(utils, parent, conditionList,
                (c) -> Collections.singletonList(new IOperation.AddOperation((itemStack) -> true,
                        GlmNodeUtils.addedNode(utils, c, item.getDefaultInstance(), 1, new RangeValue(min, max)))));
    }
}

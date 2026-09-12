package com.yanny.alicompat.compat.moonlight;

import com.yanny.aci.api.RangeValue;
import com.yanny.ali.api.ILootModifier;
import com.yanny.ali.api.IOperation;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.plugin.glm.GlobalLootModifierUtils;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.GlmNodeUtils;
import com.yanny.alicompat.accessor.IGlobalLootModifierAccessor;
import net.mehvahdjukaar.moonlight.core.misc.forge.ModLootModifiers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class AddItemModifierAccessor extends BaseAccessor<ModLootModifiers.AddItemModifier> implements IGlobalLootModifierAccessor {
    @FieldAccessor
    private ItemStack addedItemStack;

    @FieldAccessor
    protected LootItemCondition[] conditions;

    public AddItemModifierAccessor(ModLootModifiers.AddItemModifier parent) {
        super(parent);
    }

    public Optional<ILootModifier<?>> getLootModifier(IServerUtils utils) {
        List<LootItemCondition> conditionList = Arrays.asList(this.conditions);

        return GlobalLootModifierUtils.getLootModifier(utils, parent, conditionList,
                (c) -> Collections.singletonList(new IOperation.AddOperation((itemStack) -> true,
                        GlmNodeUtils.addedNode(utils, c, addedItemStack.copy(), 1, new RangeValue(addedItemStack.getCount())))));
    }
}

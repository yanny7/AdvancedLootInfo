package com.yanny.alicompat.compat.cognition;

import com.cyanogen.experienceobelisk.loot_modifiers.AddSingleItem;
import com.yanny.aci.api.RangeValue;
import com.yanny.ali.api.ILootModifier;
import com.yanny.ali.api.IOperation;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.plugin.glm.Destination;
import com.yanny.ali.plugin.glm.GlobalLootModifierUtils;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.GlmNodeUtils;
import com.yanny.alicompat.accessor.IDestination;
import com.yanny.alicompat.accessor.IGlobalLootModifierAccessor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class AddSingleItemAccessor extends BaseAccessor<AddSingleItem> implements IGlobalLootModifierAccessor, IDestination {
    @FieldAccessor
    protected LootItemCondition[] conditions;

    public AddSingleItemAccessor(AddSingleItem parent) {
        super(parent);
    }

    @Override
    public Optional<ILootModifier<?>> getLootModifier(IServerUtils utils) {
        Item item = parent.item;
        float appearChance = parent.appearChance;
        RangeValue count = new RangeValue(parent.min, parent.max);

        return GlobalLootModifierUtils.getLootModifier(utils, parent, Arrays.asList(this.conditions),
                (c) -> List.of(new IOperation.AddOperation((itemStack) -> true, GlmNodeUtils.addedNode(utils, c, item.getDefaultInstance(), appearChance, count))));
    }

    @NotNull
    @Override
    public Destination getDestination(IServerUtils ignoredUtils) {
        String path = parent.path;

        return new Destination.Table((id) -> id.getPath().contains(path), false);
    }
}

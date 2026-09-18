package com.yanny.alicompat.compat.cognition;

import com.cyanogen.experienceobelisk.loot_modifiers.AddSingleItem;
import com.yanny.aci.api.RangeValue;
import com.yanny.ali.api.IOperation;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.plugin.glm.GlobalLootModifierUtils;
import com.yanny.ali.plugin.glm.IPageLootModifier;
import com.yanny.ali.plugin.glm.LootPage;
import com.yanny.ali.plugin.glm.Verdict;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.GlmNodeUtils;
import com.yanny.alicompat.accessor.IGlobalLootModifierAccessor;
import com.yanny.alicompat.accessor.IPageResolverAccessor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class AddSingleItemAccessor extends BaseAccessor<AddSingleItem> implements IGlobalLootModifierAccessor, IPageResolverAccessor {
    @FieldAccessor
    protected LootItemCondition[] conditions;

    public AddSingleItemAccessor(AddSingleItem parent) {
        super(parent);
    }

    @Override
    public Optional<IPageLootModifier> getLootModifier(IServerUtils utils) {
        Item item = parent.item;
        float appearChance = parent.appearChance;
        RangeValue count = new RangeValue(parent.min, parent.max);

        return Optional.of(GlobalLootModifierUtils.getLootModifier(utils, parent, Arrays.asList(this.conditions),
                (c) -> List.of(new IOperation.AddOperation((itemStack) -> true, GlmNodeUtils.addedNode(utils, c, item.getDefaultInstance(), appearChance, count)))));
    }

    @NotNull
    @Override
    public Verdict test(IServerUtils ignoredUtils, LootPage page) {
        String path = parent.path;

        return GlobalLootModifierUtils.testTable(page, (id) -> id.getPath().contains(path), false);
    }
}

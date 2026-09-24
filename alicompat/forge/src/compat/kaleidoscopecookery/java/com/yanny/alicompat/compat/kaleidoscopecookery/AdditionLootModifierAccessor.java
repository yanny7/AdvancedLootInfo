package com.yanny.alicompat.compat.kaleidoscopecookery;

import com.github.ysbbbbbb.kaleidoscopecookery.loot.AdditionLootModifier;
import com.yanny.ali.api.IDataNode;
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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;

public class AdditionLootModifierAccessor extends BaseAccessor<AdditionLootModifier> implements IGlobalLootModifierAccessor, IPageResolverAccessor {
    @FieldAccessor
    private ResourceLocation lootTableType;
    @FieldAccessor
    @Nullable
    private ResourceLocation lootTableId;
    @FieldAccessor
    private ResourceLocation lootTableAdd;
    @FieldAccessor
    protected LootItemCondition[] conditions;

    public AdditionLootModifierAccessor(AdditionLootModifier parent) {
        super(parent);
    }

    @Override
    public Optional<IPageLootModifier> getLootModifier(IServerUtils utils) {
        return Optional.of(GlobalLootModifierUtils.getLootModifier(utils, parent, Arrays.asList(this.conditions), (c) -> {
            IDataNode node = GlmNodeUtils.referenceNode(utils, c, lootTableAdd);
            return Collections.singletonList(new IOperation.AddOperation((itemStack) -> true, node));
        }));
    }

    @NotNull
    @Override
    public Verdict test(IServerUtils ignoredUtils, LootPage page) {
        if (page.tableId().equals(lootTableAdd) || !Objects.equals(page.paramSet(), LootContextParamSets.get(lootTableType))) {
            return Verdict.NO;
        }

        return GlobalLootModifierUtils.testTable(page, (id) -> lootTableId == null || id.equals(lootTableId), true);
    }
}

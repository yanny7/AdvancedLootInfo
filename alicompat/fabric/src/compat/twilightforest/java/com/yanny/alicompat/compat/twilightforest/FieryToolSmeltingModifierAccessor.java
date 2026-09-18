package com.yanny.alicompat.compat.twilightforest;

import com.yanny.ali.api.IOperation;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.plugin.glm.GlobalLootModifierUtils;
import com.yanny.ali.plugin.glm.IPageLootModifier;
import com.yanny.ali.plugin.glm.LootPage;
import com.yanny.ali.plugin.glm.Verdict;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.IGlobalLootModifierAccessor;
import com.yanny.alicompat.accessor.IPageResolverAccessor;
import com.yanny.alicompat.accessor.SmeltingUtils;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;
import twilightforest.loot.modifiers.FieryToolSmeltingModifier;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

public class FieryToolSmeltingModifierAccessor extends BaseAccessor<FieryToolSmeltingModifier> implements IGlobalLootModifierAccessor, IPageResolverAccessor {
    @FieldAccessor
    protected LootItemCondition[] conditions;

    public FieryToolSmeltingModifierAccessor(FieryToolSmeltingModifier parent) {
        super(parent);
    }

    @Override
    public Optional<IPageLootModifier> getLootModifier(IServerUtils utils) {
        return Optional.of(GlobalLootModifierUtils.getLootModifier(utils, parent, Arrays.asList(this.conditions),
                (c) -> Collections.singletonList(new IOperation.ReplaceOperation(
                        (itemStack) -> SmeltingUtils.smelt(utils, itemStack).isPresent(),
                        (src) -> SmeltingUtils.smeltedNode(utils, c, src)))));
    }

    @NotNull
    @Override
    public Verdict test(IServerUtils ignoredUtils, LootPage page) {
        return GlobalLootModifierUtils.testBlocks(page, (block) -> true, false);
    }
}

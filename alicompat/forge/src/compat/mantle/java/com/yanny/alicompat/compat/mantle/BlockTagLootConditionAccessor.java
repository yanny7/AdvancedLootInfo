package com.yanny.alicompat.compat.mantle;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import com.yanny.ali.plugin.glm.GlobalLootModifierUtils;
import com.yanny.ali.plugin.glm.LootPage;
import com.yanny.ali.plugin.glm.Verdict;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.IConditionTooltip;
import com.yanny.alicompat.accessor.IPageResolverAccessor;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import slimeknights.mantle.loot.condition.BlockTagLootCondition;

public class BlockTagLootConditionAccessor extends BaseAccessor<BlockTagLootCondition> implements IConditionTooltip, IPageResolverAccessor {
    @FieldAccessor
    private TagKey<Block> tag;

    @FieldAccessor
    private StatePropertiesPredicate properties;

    public BlockTagLootConditionAccessor(BlockTagLootCondition parent) {
        super(parent);
    }

    @NotNull
    @Override
    public TooltipBuilder getTooltip(IServerUtils utils) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, tag).build(Lang.Value.TAG));
            b.add(utils.getValueTooltip(utils, properties).build(Lang.Branch.PROPERTIES));
        }, MantleLang.Conditions.BLOCK_TAG);
    }

    @Nullable
    @Override
    public Verdict test(IServerUtils utils, LootPage page) {
        if (tag == null || page.blocks().isEmpty()) {
            return null;
        }

        return GlobalLootModifierUtils.testBlocks(page, (block) -> block.builtInRegistryHolder().is(tag), properties.properties.isEmpty());
    }
}

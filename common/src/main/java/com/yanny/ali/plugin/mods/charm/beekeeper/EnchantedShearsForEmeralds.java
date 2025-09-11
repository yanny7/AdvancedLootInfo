package com.yanny.ali.plugin.mods.charm.beekeeper;

import com.mojang.datafixers.util.Either;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import com.yanny.ali.api.RangeValue;
import com.yanny.ali.plugin.common.trades.ItemsToItemsNode;
import com.yanny.ali.plugin.mods.ClassAccessor;
import com.yanny.ali.plugin.mods.FieldAccessor;
import com.yanny.ali.plugin.mods.IItemListing;
import com.yanny.ali.plugin.server.RegistriesTooltipUtils;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.IntRange;
import org.jetbrains.annotations.NotNull;
import oshi.util.tuples.Pair;

import java.util.Collections;
import java.util.List;

import static com.yanny.ali.plugin.server.GenericTooltipUtils.getIntRangeTooltip;

@ClassAccessor("svenhjol.charm.feature.beekeepers.BeekeeperTradeOffers$EnchantedShearsForEmeralds")
public class EnchantedShearsForEmeralds implements IItemListing {
    @FieldAccessor
    private int villagerXp;
    @FieldAccessor
    private int baseEmeralds;
    @FieldAccessor
    private int extraEmeralds;
    @FieldAccessor
    private int maxUses;

    @NotNull
    @Override
    public IDataNode getNode(IServerUtils utils, List<ITooltipNode> conditions) {
        ITooltipNode tooltip = RegistriesTooltipUtils.getEnchantmentTooltip(utils, "ali.property.value.enchantment", Enchantments.UNBREAKING);

        tooltip.add(getIntRangeTooltip(utils, "ali.property.value.levels", IntRange.range(1, 3)));

        return new ItemsToItemsNode(
                utils,
                Either.left(Items.EMERALD.getDefaultInstance()),
                new RangeValue(baseEmeralds, baseEmeralds + 3 * 3 + extraEmeralds),
                Collections.emptyList(),
                Either.left(ItemStack.EMPTY),
                new RangeValue(),
                Collections.emptyList(),
                Either.left(Items.SHEARS.getDefaultInstance()),
                new RangeValue(),
                List.of(tooltip),
                maxUses,
                villagerXp,
                0.2F,
                conditions
        );
    }

    @Override
    public Pair<List<Item>, List<Item>> collectItems(IServerUtils utils) {
        return new Pair<>(List.of(Items.EMERALD), List.of(Items.SHEARS));
    }
}

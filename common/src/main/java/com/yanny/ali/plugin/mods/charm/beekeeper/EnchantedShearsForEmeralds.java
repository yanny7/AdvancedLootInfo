package com.yanny.ali.plugin.mods.charm.beekeeper;

import com.mojang.datafixers.util.Either;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import com.yanny.ali.api.RangeValue;
import com.yanny.ali.plugin.common.trades.ItemsToItemsNode;
import com.yanny.ali.plugin.mods.BaseAccessor;
import com.yanny.ali.plugin.mods.ClassAccessor;
import com.yanny.ali.plugin.mods.FieldAccessor;
import com.yanny.ali.plugin.mods.IItemListing;
import com.yanny.ali.plugin.server.RegistriesTooltipUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.npc.VillagerTrades;
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

@ClassAccessor("svenhjol.charm.feature.beekeepers.common.Trades$EnchantedShearsForEmeralds")
public class EnchantedShearsForEmeralds extends BaseAccessor<VillagerTrades.ItemListing> implements IItemListing {
    @FieldAccessor
    private int villagerXp;
    @FieldAccessor
    private int baseEmeralds;
    @FieldAccessor
    private int extraEmeralds;
    @FieldAccessor
    private int maxUses;

    public EnchantedShearsForEmeralds(VillagerTrades.ItemListing parent) {
        super(parent);
    }

    @NotNull
    @Override
    public IDataNode getNode(IServerUtils utils, List<ITooltipNode> conditions) {
        ITooltipNode tooltip = RegistriesTooltipUtils.getEnchantmentTooltip(utils, "ali.property.value.enchantment", utils.lookupProvider().lookupOrThrow(Registries.ENCHANTMENT).get(Enchantments.UNBREAKING).orElseThrow().value());

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

package com.yanny.ali.plugin.charm_forked;

import com.mojang.datafixers.util.Either;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import com.yanny.ali.api.RangeValue;
import com.yanny.ali.mixin.*;
import com.yanny.ali.plugin.common.trades.ItemsToItemsNode;
import com.yanny.ali.plugin.common.trades.SubTradesNode;
import com.yanny.ali.plugin.server.RegistriesTooltipUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.IntRange;
import org.jetbrains.annotations.NotNull;
import oshi.util.tuples.Pair;
import svenhjol.charm.CharmTags;
import svenhjol.charm.feature.beekeepers.BeekeeperTradeOffers;
import svenhjol.charm.feature.lumberjacks.LumberjackTradeOffers;
import svenhjol.charmony.helper.GenericTradeOffers;
import svenhjol.charmony.helper.TagHelper;

import java.util.*;
import java.util.stream.Stream;

import static com.yanny.ali.plugin.server.GenericTooltipUtils.getIntRangeTooltip;
import static com.yanny.ali.plugin.server.GenericTooltipUtils.getIntegerTooltip;

public class TradeUtils {
    @NotNull
    public static IDataNode getNode(IServerUtils utils, BeekeeperTradeOffers.EmeraldsForFlowers entry, List<ITooltipNode> conditions) {
        MixinEmeraldForFlowers mixin = (MixinEmeraldForFlowers) entry;
        return new ItemsToItemsNode(
                utils,
                Either.right(CharmTags.BEEKEEPER_SELLS_FLOWERS),
                new RangeValue(mixin.getBaseCost(), mixin.getBaseCost() + mixin.getExtraCost()),
                Either.left(Items.EMERALD.getDefaultInstance()),
                new RangeValue(mixin.getBaseEmeralds(), mixin.getBaseEmeralds() + mixin.getExtraEmeralds()),
                mixin.getMaxUses(),
                mixin.getVillagerXp(),
                0.2F,
                conditions
        );
    }

    @NotNull
    public static IDataNode getNode(IServerUtils utils, BeekeeperTradeOffers.TallFlowerForEmeralds entry, List<ITooltipNode> conditions) {
        MixinTallFlowerForEmeralds mixin = (MixinTallFlowerForEmeralds) entry;

        return new SubTradesNode<>(utils, entry, conditions) {
            @Override
            public List<IDataNode> getSubTrades(IServerUtils utils, BeekeeperTradeOffers.TallFlowerForEmeralds listing) {
                List<Item> flowers = List.of(Items.SUNFLOWER, Items.PEONY, Items.LILAC, Items.ROSE_BUSH);
                List<IDataNode> nodes = new ArrayList<>();

                for (Item flower : flowers) {
                    nodes.add(new ItemsToItemsNode(
                            utils,
                            Either.left(Items.EMERALD.getDefaultInstance()),
                            new RangeValue(mixin.getBaseEmeralds(), mixin.getBaseEmeralds() + mixin.getExtraEmeralds()),
                            Either.left(flower.getDefaultInstance()),
                            new RangeValue(),
                            mixin.getMaxUses(),
                            mixin.getVillagerXp(),
                            0.2F,
                            Collections.emptyList()
                    ));
                }

                return nodes;
            }
        };
    }

    @NotNull
    public static IDataNode getNode(IServerUtils utils, BeekeeperTradeOffers.EnchantedShearsForEmeralds entry, List<ITooltipNode> conditions) {
        MixinEnchantedShearsForEmeralds mixin = (MixinEnchantedShearsForEmeralds) entry;
        ITooltipNode tooltip = RegistriesTooltipUtils.getEnchantmentTooltip(utils, "ali.property.value.enchantment", Enchantments.UNBREAKING);

        tooltip.add(getIntRangeTooltip(utils, "ali.property.value.levels", IntRange.range(1, 3)));

        return new ItemsToItemsNode(
                utils,
                Either.left(Items.EMERALD.getDefaultInstance()),
                new RangeValue(mixin.getBaseEmeralds(), mixin.getBaseEmeralds() + 3 * 3 + mixin.getExtraEmeralds()),
                Collections.emptyList(),
                Either.left(ItemStack.EMPTY),
                new RangeValue(),
                Collections.emptyList(),
                Either.left(Items.SHEARS.getDefaultInstance()),
                new RangeValue(),
                List.of(tooltip),
                mixin.getMaxUses(),
                mixin.getVillagerXp(),
                0.2F,
                conditions
        );
    }

    @NotNull
    public static IDataNode getNode(IServerUtils utils, BeekeeperTradeOffers.PopulatedBeehiveForEmeralds entry, List<ITooltipNode> conditions) {
        MixinPopulatedBeehiveForEmeralds mixin = (MixinPopulatedBeehiveForEmeralds) entry;
        ITooltipNode tooltip = RegistriesTooltipUtils.getEnchantmentTooltip(utils, "ali.property.branch.bees", Enchantments.UNBREAKING);

        tooltip.add(getIntegerTooltip(utils, "ali.property.value.count", 2));

        return new ItemsToItemsNode(
                utils,
                Either.left(Items.EMERALD.getDefaultInstance()),
                new RangeValue(mixin.getBaseEmeralds(), mixin.getBaseEmeralds() + mixin.getExtraEmeralds()),
                Collections.emptyList(),
                Either.left(ItemStack.EMPTY),
                new RangeValue(),
                Collections.emptyList(),
                Either.left(Items.BEEHIVE.getDefaultInstance()),
                new RangeValue(),
                List.of(tooltip),
                mixin.getMaxUses(),
                mixin.getVillagerXp(),
                0.2F,
                conditions
        );
    }

    @NotNull
    public static IDataNode getNode(IServerUtils utils, LumberjackTradeOffers.SaplingsForEmeralds entry, List<ITooltipNode> conditions) {
        MixinSaplingsForEmeralds mixin = (MixinSaplingsForEmeralds) entry;

        return new SubTradesNode<>(utils, entry, conditions) {
            @Override
            public List<IDataNode> getSubTrades(IServerUtils utils, LumberjackTradeOffers.SaplingsForEmeralds listing) {
                List<IDataNode> nodes = new ArrayList<>();

                for (Item sapling : mixin.getSaplings()) {
                    nodes.add(new ItemsToItemsNode(
                            utils,
                            Either.left(Items.EMERALD.getDefaultInstance()),
                            new RangeValue(mixin.getBaseEmeralds(), mixin.getBaseEmeralds() + mixin.getExtraEmeralds()),
                            Either.left(sapling.getDefaultInstance()),
                            new RangeValue(),
                            mixin.getMaxUses(),
                            mixin.getVillagerXp(),
                            0.2F,
                            Collections.emptyList()
                    ));
                }

                return nodes;
            }
        };
    }

    @NotNull
    public static IDataNode getNode(IServerUtils utils, LumberjackTradeOffers.BarkForLogs entry, List<ITooltipNode> conditions) {
        MixinBarkForLogs mixin = (MixinBarkForLogs) entry;

        return new SubTradesNode<>(utils, entry, conditions) {
            @Override
            public List<IDataNode> getSubTrades(IServerUtils utils, LumberjackTradeOffers.BarkForLogs listing) {
                List<IDataNode> nodes = new ArrayList<>();
                Map<Block, Block> map = new HashMap<>();

                map.put(Blocks.ACACIA_LOG, Blocks.ACACIA_WOOD);
                map.put(Blocks.BIRCH_LOG, Blocks.BIRCH_WOOD);
                map.put(Blocks.DARK_OAK_LOG, Blocks.DARK_OAK_WOOD);
                map.put(Blocks.JUNGLE_LOG, Blocks.JUNGLE_WOOD);
                map.put(Blocks.MANGROVE_LOG, Blocks.MANGROVE_WOOD);
                map.put(Blocks.OAK_LOG, Blocks.OAK_WOOD);
                map.put(Blocks.SPRUCE_LOG, Blocks.SPRUCE_WOOD);

                for (Map.Entry<Block, Block> e : map.entrySet()) {
                    nodes.add(new ItemsToItemsNode(
                            utils,
                            Either.left(Items.EMERALD.getDefaultInstance()),
                            new RangeValue(1),
                            Either.left(e.getKey().asItem().getDefaultInstance()),
                            new RangeValue(mixin.getBaseCost(), mixin.getBaseCost() + mixin.getExtraCost()),
                            Either.left(e.getValue().asItem().getDefaultInstance()),
                            new RangeValue(mixin.getBaseCost(), mixin.getBaseCost() + mixin.getExtraCost()),
                            mixin.getMaxUses(),
                            mixin.getVillagerXp(),
                            0.2F,
                            Collections.emptyList()
                    ));
                }

                return nodes;
            }
        };
    }

    @NotNull
    public static <T extends ItemLike> IDataNode getNode(IServerUtils utils, GenericTradeOffers.EmeraldsForTag<T> entry, List<ITooltipNode> conditions) {
        //noinspection unchecked
        MixinEmeraldsForTag<T> mixin = (MixinEmeraldsForTag<T>) entry;

        return new ItemsToItemsNode(
                utils,
                Either.right(mixin.getTag()),
                new RangeValue(mixin.getBaseCost(), mixin.getBaseCost() + mixin.getExtraCost()),
                Either.left(Items.EMERALD.getDefaultInstance()),
                new RangeValue(mixin.getBaseEmeralds(), mixin.getBaseEmeralds() + mixin.getExtraEmeralds()),
                mixin.getMaxUses(),
                mixin.getVillagerXp(),
                0.05F,
                conditions
        );
    }

    @NotNull
    public static <T extends ItemLike, U extends ItemLike> IDataNode getNode(IServerUtils utils, GenericTradeOffers.EmeraldsForTwoTags<T, U> entry, List<ITooltipNode> conditions) {
        //noinspection unchecked
        MixinEmeraldsForTwoTags<T, U> mixin = (MixinEmeraldsForTwoTags<T, U>) entry;

        return new ItemsToItemsNode(
                utils,
                Either.right(mixin.getTag1()),
                new RangeValue(mixin.getBaseCost(), mixin.getBaseCost() + mixin.getExtraCost()),
                Either.right(mixin.getTag2()),
                new RangeValue(mixin.getBaseCost(), mixin.getBaseCost() + mixin.getExtraCost()),
                Either.left(Items.EMERALD.getDefaultInstance()),
                new RangeValue(mixin.getBaseEmeralds(), mixin.getBaseEmeralds() + mixin.getExtraEmeralds()),
                mixin.getMaxUses(),
                mixin.getVillagerXp(),
                0.05F,
                conditions
        );
    }

    @NotNull
    public static IDataNode getNode(IServerUtils utils, GenericTradeOffers.EmeraldsForItems entry, List<ITooltipNode> conditions) {
        MixinEmeraldsForItems mixin = (MixinEmeraldsForItems) entry;

        return new ItemsToItemsNode(
                utils,
                Either.left(mixin.getItemLike().asItem().getDefaultInstance()),
                new RangeValue(mixin.getBaseCost(), mixin.getBaseCost() + mixin.getExtraCost()),
                Either.left(Items.EMERALD.getDefaultInstance()),
                new RangeValue(mixin.getBaseEmeralds(), mixin.getBaseEmeralds() + mixin.getExtraEmeralds()),
                mixin.getMaxUses(),
                mixin.getVillagerXp(),
                0.05F,
                conditions
        );
    }

    @NotNull
    public static <T extends ItemLike> IDataNode getNode(IServerUtils utils, GenericTradeOffers.TagForEmeralds<T> entry, List<ITooltipNode> conditions) {
        //noinspection unchecked
        MixinTagForEmeralds<T> mixin = (MixinTagForEmeralds<T>) entry;

        return new ItemsToItemsNode(
                utils,
                Either.left(Items.EMERALD.getDefaultInstance()),
                new RangeValue(mixin.getBaseEmeralds(), mixin.getBaseEmeralds() + mixin.getExtraEmeralds()),
                Either.right(mixin.getTag()),
                new RangeValue(mixin.getBaseItems(), mixin.getBaseItems() + mixin.getExtraItems()),
                mixin.getMaxUses(),
                mixin.getVillagerXp(),
                0.05F,
                conditions
        );
    }

    @NotNull
    public static IDataNode getNode(IServerUtils utils, GenericTradeOffers.ItemsForEmeralds entry, List<ITooltipNode> conditions) {
        MixinItemsForEmeralds mixin = (MixinItemsForEmeralds) entry;

        return new ItemsToItemsNode(
                utils,
                Either.left(Items.EMERALD.getDefaultInstance()),
                new RangeValue(mixin.getBaseEmeralds(), mixin.getBaseEmeralds() + mixin.getExtraEmeralds()),
                Either.left(mixin.getItemLike().asItem().getDefaultInstance()),
                new RangeValue(mixin.getBaseItems(), mixin.getBaseItems() + mixin.getExtraItems()),
                mixin.getMaxUses(),
                mixin.getVillagerXp(),
                0.05F,
                conditions
        );
    }

    @NotNull
    public static IDataNode getNode(IServerUtils utils, GenericTradeOffers.ItemsForItems entry, List<ITooltipNode> conditions) {
        MixinItemsForItems mixin = (MixinItemsForItems) entry;

        return new ItemsToItemsNode(
                utils,
                Either.left(mixin.getInputItem().asItem().getDefaultInstance()),
                new RangeValue(mixin.getBaseInput(), mixin.getBaseInput() + mixin.getExtraInput()),
                Either.left(mixin.getOutputItem().asItem().getDefaultInstance()),
                new RangeValue(mixin.getBaseOutput(), mixin.getBaseOutput() + mixin.getExtraOutput()),
                mixin.getMaxUses(),
                mixin.getVillagerXp(),
                0.05F,
                conditions
        );
    }

    @NotNull
    public static IDataNode getNode(IServerUtils utils, MixinAnvilRepair entry, List<ITooltipNode> conditions) {
        return new SubTradesNode<>(utils, entry, conditions) {
            @Override
            public List<IDataNode> getSubTrades(IServerUtils utils, MixinAnvilRepair listing) {
                List<Item> items = List.of(Items.DAMAGED_ANVIL, Items.CHIPPED_ANVIL);
                List<IDataNode> nodes = new ArrayList<>();

                for (Item item : items) {
                    nodes.add(new ItemsToItemsNode(
                            utils,
                            Either.left(item.getDefaultInstance()),
                            new RangeValue(),
                            Either.left(Items.IRON_INGOT.getDefaultInstance()),
                            new RangeValue(5, 9),
                            Either.left(Items.ANVIL.getDefaultInstance()),
                            new RangeValue(),
                            entry.getMaxUses(),
                            entry.getVillagerXp(),
                            0.2F,
                            Collections.emptyList()
                    ));
                }

                return nodes;
            }
        };
    }

    @NotNull
    public static Pair<List<Item>, List<Item>> collectItems(IServerUtils utils, BeekeeperTradeOffers.EmeraldsForFlowers ignoredListing) {
        ServerLevel level = utils.getServerLevel();

        if (level != null) {
            TagKey<Item> tag = CharmTags.BEEKEEPER_SELLS_FLOWERS;
            return new Pair<>(
                    TagHelper.getValues(level.registryAccess().registryOrThrow(tag.registry()), tag).stream().map(ItemLike::asItem).toList(),
                    List.of(Items.EMERALD)
            );
        } else {
            return new Pair<>(Collections.emptyList(), Collections.emptyList());
        }
    }

    @NotNull
    public static Pair<List<Item>, List<Item>> collectItems(IServerUtils ignoredUtils, BeekeeperTradeOffers.EnchantedShearsForEmeralds ignoredListing) {
        return new Pair<>(
                List.of(Items.EMERALD),
                List.of(Items.SHEARS)
        );
    }

    @NotNull
    public static Pair<List<Item>, List<Item>> collectItems(IServerUtils ignoredUtils, BeekeeperTradeOffers.PopulatedBeehiveForEmeralds ignoredListing) {
        return new Pair<>(
                List.of(Items.EMERALD),
                List.of(Items.BEEHIVE)
        );
    }

    @NotNull
    public static Pair<List<Item>, List<Item>> collectItems(IServerUtils ignoredUtils, BeekeeperTradeOffers.TallFlowerForEmeralds ignoredListing) {
        return new Pair<>(
                List.of(Items.EMERALD),
                List.of(Items.SUNFLOWER, Items.PEONY, Items.LILAC, Items.ROSE_BUSH)
        );
    }

    @NotNull
    public static Pair<List<Item>, List<Item>> collectItems(IServerUtils ignoredUtils, LumberjackTradeOffers.SaplingsForEmeralds listing) {
        return new Pair<>(
                List.of(Items.EMERALD),
                ((MixinSaplingsForEmeralds) listing).getSaplings()
        );
    }

    @NotNull
    public static Pair<List<Item>, List<Item>> collectItems(IServerUtils ignoredUtils, LumberjackTradeOffers.BarkForLogs ignoredListing) {
        return new Pair<>(
                List.of(Items.EMERALD, Items.ACACIA_LOG, Items.BIRCH_LOG, Items.DARK_OAK_LOG, Items.JUNGLE_LOG, Items.MANGROVE_LOG, Items.OAK_LOG, Items.SPRUCE_LOG),
                List.of(Items.ACACIA_WOOD, Items.BIRCH_WOOD, Items.DARK_OAK_WOOD, Items.JUNGLE_WOOD, Items.MANGROVE_WOOD, Items.OAK_WOOD, Items.SPRUCE_WOOD)
        );
    }

    @NotNull
    public static <T extends ItemLike> Pair<List<Item>, List<Item>> collectItems(IServerUtils utils, GenericTradeOffers.EmeraldsForTag<T> listing) {
        ServerLevel level = utils.getServerLevel();

        if (level != null) {
            //noinspection unchecked
            TagKey<T> tag = ((MixinEmeraldsForTag<T>) listing).getTag();
            return new Pair<>(
                    TagHelper.getValues(level.registryAccess().registryOrThrow(tag.registry()), tag).stream().map(ItemLike::asItem).toList(),
                    List.of(Items.EMERALD)
            );
        } else {
            return new Pair<>(Collections.emptyList(), Collections.emptyList());
        }
    }

    @NotNull
    public static <T extends ItemLike, U extends ItemLike> Pair<List<Item>, List<Item>> collectItems(IServerUtils utils, GenericTradeOffers.EmeraldsForTwoTags<T, U> listing) {
        ServerLevel level = utils.getServerLevel();

        if (level != null) {
            //noinspection unchecked
            TagKey<T> tag1 = ((MixinEmeraldsForTwoTags<T, U>) listing).getTag1();
            //noinspection unchecked
            TagKey<U> tag2 = ((MixinEmeraldsForTwoTags<T, U>) listing).getTag2();
            return new Pair<>(
                    Stream.concat(
                            TagHelper.getValues(level.registryAccess().registryOrThrow(tag1.registry()), tag1).stream().map(ItemLike::asItem),
                            TagHelper.getValues(level.registryAccess().registryOrThrow(tag2.registry()), tag2).stream().map(ItemLike::asItem)
                    ).toList(),
                    List.of(Items.EMERALD)
            );
        } else {
            return new Pair<>(Collections.emptyList(), Collections.emptyList());
        }
    }

    @NotNull
    public static Pair<List<Item>, List<Item>> collectItems(IServerUtils ignoredUtils, GenericTradeOffers.EmeraldsForItems listing) {
        return new Pair<>(
                List.of(((MixinEmeraldsForItems) listing).getItemLike().asItem()),
                List.of(Items.EMERALD)
        );
    }

    @NotNull
    public static <T extends ItemLike> Pair<List<Item>, List<Item>> collectItems(IServerUtils utils, GenericTradeOffers.TagForEmeralds<T> listing) {
        ServerLevel level = utils.getServerLevel();

        if (level != null) {
            //noinspection unchecked
            TagKey<T> tag = ((MixinTagForEmeralds<T>) listing).getTag();
            return new Pair<>(
                    List.of(Items.EMERALD),
                    TagHelper.getValues(level.registryAccess().registryOrThrow(tag.registry()), tag).stream().map(ItemLike::asItem).toList()
            );
        } else {
            return new Pair<>(Collections.emptyList(), Collections.emptyList());
        }
    }

    @NotNull
    public static Pair<List<Item>, List<Item>> collectItems(IServerUtils ignoredUtils, GenericTradeOffers.ItemsForEmeralds listing) {
        return new Pair<>(
                List.of(Items.EMERALD),
                List.of(((MixinItemsForEmeralds) listing).getItemLike().asItem())
        );
    }

    @NotNull
    public static Pair<List<Item>, List<Item>> collectItems(IServerUtils ignoredUtils, GenericTradeOffers.ItemsForItems listing) {
        return new Pair<>(
                List.of(((MixinItemsForItems) listing).getInputItem().asItem()),
                List.of(((MixinItemsForItems) listing).getOutputItem().asItem())
        );
    }

    @NotNull
    public static Pair<List<Item>, List<Item>> collectItems(IServerUtils ignoredUtils, MixinAnvilRepair ignoredListing) {
        return new Pair<>(
                List.of(Items.DAMAGED_ANVIL, Items.CHIPPED_ANVIL, Items.IRON_INGOT),
                List.of(Items.ANVIL)
        );
    }
}

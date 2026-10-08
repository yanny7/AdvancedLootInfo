package com.yanny.alicompat.compat.ironsspellbooks;

import com.yanny.aci.CommonLogUtils;
import com.yanny.aci.api.NumberExpr;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.TradeLevelInfo;
import com.yanny.ali.plugin.common.trades.TradeUtils;
import com.yanny.alicompat.Utils;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import io.redspace.ironsspellbooks.entity.mobs.wizards.alchemist.ApothecaristEntity;
import io.redspace.ironsspellbooks.item.FurledMapItem;
import io.redspace.ironsspellbooks.item.InkItem;
import io.redspace.ironsspellbooks.loot.SpellFilter;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.component.Fireworks;
import net.minecraft.world.item.trading.MerchantOffer;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class WizardTrades {
    private static final Logger LOGGER = CommonLogUtils.getLogger(Utils.MOD_ID);

    public static final ResourceLocation APOTHECARIST = ResourceLocation.fromNamespaceAndPath(IronsSpellbooksLang.MOD_ID, "apothecarist");
    public static final ResourceLocation CRYOMANCER = ResourceLocation.fromNamespaceAndPath(IronsSpellbooksLang.MOD_ID, "cryomancer");
    public static final ResourceLocation PRIEST = ResourceLocation.fromNamespaceAndPath(IronsSpellbooksLang.MOD_ID, "priest");
    public static final ResourceLocation PYROMANCER = ResourceLocation.fromNamespaceAndPath(IronsSpellbooksLang.MOD_ID, "pyromancer");

    private static final TradeLevelInfo ALL = new TradeLevelInfo(NumberExpr.constant(Integer.MAX_VALUE));
    private static final TradeLevelInfo ONE = new TradeLevelInfo(NumberExpr.constant(1));
    private static final TradeLevelInfo INK = new TradeLevelInfo(NumberExpr.constant(1), 0.25f);

    @NotNull
    public static Int2ObjectMap<VillagerTrades.ItemListing[]> apothecarist() {
        Int2ObjectMap<VillagerTrades.ItemListing[]> listings = new Int2ObjectArrayMap<>();
        SchoolType nature = SchoolRegistry.NATURE.get();

        listings.put(1, fillerOffers().stream().map(WizardTrade::of).toArray(VillagerTrades.ItemListing[]::new));
        listings.put(2, listings(inkBuy(ink(ItemRegistry.INK_UNCOMMON))));
        listings.put(3, listings(inkBuy(ink(ItemRegistry.INK_RARE))));
        listings.put(4, listings(inkBuy(ink(ItemRegistry.INK_EPIC))));
        listings.put(5, elixirBuy(false));
        listings.put(6, potionOrElixirSell());
        listings.put(7, listings(randomScroll(nature, 0.0f, 0.4f)));
        listings.put(8, listings(randomScroll(nature, 0.5f, 0.9f)));
        listings.put(9, listings(WizardTrade.of(emeralds(), NumberExpr.constant(16), stack(ItemRegistry.NETHERWARD_TINCTURE), NumberExpr.constant(1), 8, 5, 0.01f)));
        listings.put(10, greaterElixirs().stream()
                .map((elixir) -> WizardTrade.of(new ItemStack(elixir, 4), NumberExpr.constant(4), stack(ItemRegistry.NATURE_RUNE), NumberExpr.constant(1), 1, 5, 0.1f))
                .toArray(VillagerTrades.ItemListing[]::new));

        return listings;
    }

    @NotNull
    public static TradeLevelInfo apothecaristLevel(int level) {
        return switch (level) {
            case 1 -> new TradeLevelInfo(NumberExpr.uniformInt(3, 4));
            case 2, 3, 4 -> INK;
            case 5 -> new TradeLevelInfo(NumberExpr.constant(1), 0.5f);
            case 6 -> new TradeLevelInfo(NumberExpr.uniformInt(1, 3));
            case 8 -> new TradeLevelInfo(NumberExpr.constant(1), 0.65f);
            case 10 -> ONE;
            default -> ALL;
        };
    }

    @NotNull
    public static Int2ObjectMap<VillagerTrades.ItemListing[]> cryomancer() {
        Int2ObjectMap<VillagerTrades.ItemListing[]> listings = new Int2ObjectArrayMap<>();
        SchoolType ice = SchoolRegistry.ICE.get();

        listings.put(1, listings(inkBuy(ink(ItemRegistry.INK_COMMON))));
        listings.put(2, listings(inkBuy(ink(ItemRegistry.INK_UNCOMMON))));
        listings.put(3, listings(inkBuy(ink(ItemRegistry.INK_RARE))));
        listings.put(4, listings(randomScroll(ice, 0.0f, 0.25f)));
        listings.put(5, listings(randomScroll(ice, 0.3f, 0.7f)));
        listings.put(6, listings(randomScroll(ice, 0.8f, 1.0f)));
        listings.put(7, listings(
                furledMap(32, "impaled_icebreaker", "item.irons_spellbooks.failed_arctic_voyage_map"),
                furledMap(32, "ice_spider_den", "item.irons_spellbooks.ice_spider_den_map")
        ));
        listings.put(8, listings(
                WizardTrade.of(new ItemStack(ItemRegistry.FIRE_ALE.get(), 4), NumberExpr.constant(4), stack(ItemRegistry.MUSIC_DISC_WHISPERS_OF_ICE), NumberExpr.constant(1), 1, 5, 0.1f),
                WizardTrade.of(new ItemStack(ItemRegistry.ICY_FANG.get(), 2), NumberExpr.constant(2), stack(ItemRegistry.ICE_RUNE), NumberExpr.constant(1), 1, 5, 0.1f)
        ));

        return listings;
    }

    @NotNull
    public static TradeLevelInfo cryomancerLevel(int level) {
        return switch (level) {
            case 1, 2, 3 -> INK;
            case 5, 6 -> new TradeLevelInfo(NumberExpr.constant(1), 0.8f);
            case 7 -> ONE;
            default -> ALL;
        };
    }

    @NotNull
    public static Int2ObjectMap<VillagerTrades.ItemListing[]> pyromancer() {
        Int2ObjectMap<VillagerTrades.ItemListing[]> listings = new Int2ObjectArrayMap<>();
        SchoolType fire = SchoolRegistry.FIRE.get();

        listings.put(1, listings(
                simpleBuy(16, new ItemStack(Items.CANDLE, 4), 1, 1),
                simpleSell(8, new ItemStack(Items.CANDLE, 4), 10, 14),
                simpleSell(8, new ItemStack(Items.FIRE_CHARGE, 3), 9, 13),
                simpleSell(12, new ItemStack(Items.LANTERN, 3), 6, 10),
                simpleBuy(16, new ItemStack(Items.HONEY_BOTTLE, 2), 3, 5),
                simpleBuy(16, new ItemStack(Items.BLAZE_ROD, 3), 4, 6),
                simpleSell(5, fireworkStack(), 3, 4)
        ));
        listings.put(2, listings(inkBuy(ink(ItemRegistry.INK_COMMON))));
        listings.put(3, listings(inkBuy(ink(ItemRegistry.INK_UNCOMMON))));
        listings.put(4, listings(inkBuy(ink(ItemRegistry.INK_RARE))));
        listings.put(5, listings(randomScroll(fire, 0.0f, 0.25f)));
        listings.put(6, listings(randomScroll(fire, 0.3f, 0.7f)));
        listings.put(7, listings(randomScroll(fire, 0.8f, 1.0f)));
        listings.put(8, listings(
                simpleSell(3, stack(ItemRegistry.FIRE_ALE), 12, 16),
                furledMap(24, "mangrove_hut", "item.irons_spellbooks.alchemical_trade_route"),
                WizardTrade.of(new ItemStack(ItemRegistry.CHAINED_BOOK.get(), 4), NumberExpr.constant(4), stack(ItemRegistry.FIRE_RUNE), NumberExpr.constant(1), 1, 5, 0.1f)
        ));

        return listings;
    }

    @NotNull
    public static TradeLevelInfo pyromancerLevel(int level) {
        return switch (level) {
            case 1 -> new TradeLevelInfo(NumberExpr.uniformInt(2, 3));
            case 2, 3, 4 -> INK;
            case 6, 7 -> new TradeLevelInfo(NumberExpr.constant(1), 0.8f);
            default -> ALL;
        };
    }

    @NotNull
    public static Int2ObjectMap<VillagerTrades.ItemListing[]> priest() {
        Int2ObjectMap<VillagerTrades.ItemListing[]> listings = new Int2ObjectArrayMap<>();

        listings.put(1, listings(
                furledMap(24, "evoker_fort", "item.irons_spellbooks.evoker_fort_battle_plans"),
                WizardTrade.of(stack(ItemRegistry.GREATER_HEALING_POTION), NumberExpr.constant(1), emeralds(), NumberExpr.constant(18), 3, 0, 0.2f),
                WizardTrade.of(emeralds(), NumberExpr.constant(6), potion(Potions.HEALING), NumberExpr.constant(1), 2, 0, 0.2f),
                WizardTrade.of(stack(ItemRegistry.TRANSLATED_ARCHEVOKER_LOGBOOK), NumberExpr.constant(1), stack(ItemRegistry.VILLAGER_SPELL_BOOK), NumberExpr.constant(1), 1, 5, 0.5f)
        ));

        return listings;
    }

    @NotNull
    public static TradeLevelInfo priestLevel(int ignoredLevel) {
        return ALL;
    }

    @NotNull
    public static WizardTrade inkBuy(InkItem ink) {
        int rarity = ink.getRarity().getValue();
        NumberExpr essence = NumberExpr.uniformInt(5 * rarity / 2 + 2, 5 * rarity / 2 + 3);

        return WizardTrade.of(new ItemStack(ink), NumberExpr.constant(1), emeralds(), NumberExpr.uniformInt(5 * rarity + 2, 5 * rarity + 3), 8, 1, 0.05f)
                .withResultSlot((utils) -> TradeUtils.getSlotNode(List.of(
                        TradeUtils.getItemSlotNode(List.of(emeralds()), NumberExpr.uniformInt(5 * rarity + 2, 5 * rarity + 3), TooltipNode.empty()),
                        TradeUtils.getItemSlotNode(List.of(new ItemStack(ItemRegistry.ARCANE_ESSENCE.get())), essence, TooltipNode.empty())
                )));
    }

    @NotNull
    public static WizardTrade inkSell(InkItem ink) {
        int rarity = ink.getRarity().getValue();

        return WizardTrade.of(emeralds(), NumberExpr.uniformInt(8 * rarity + 2, 8 * rarity + 3), new ItemStack(ink), NumberExpr.constant(1), 4, 1, 0.05f);
    }

    @NotNull
    private static VillagerTrades.ItemListing[] elixirBuy(boolean greater) {
        int multiplier = greater ? 2 : 1;

        return elixirs(greater).stream()
                .map((elixir) -> WizardTrade.of(new ItemStack(elixir), NumberExpr.constant(1), emeralds(),
                        NumberExpr.add(NumberExpr.constant(6), NumberExpr.mul(NumberExpr.constant(multiplier), NumberExpr.uniformInt(3, 6))), 6, 1, 0.05f))
                .toArray(VillagerTrades.ItemListing[]::new);
    }

    @NotNull
    private static VillagerTrades.ItemListing[] potionOrElixirSell() {
        List<VillagerTrades.ItemListing> trades = new ArrayList<>();

        trades.add(potionSell());
        trades.addAll(elixirSell(false));
        return trades.toArray(new VillagerTrades.ItemListing[0]);
    }

    @NotNull
    private static List<WizardTrade> elixirSell(boolean greater) {
        int multiplier = greater ? 2 : 1;

        return elixirs(greater).stream()
                .map((elixir) -> WizardTrade.of(emeralds(),
                        NumberExpr.add(NumberExpr.constant(10), NumberExpr.mul(NumberExpr.constant(multiplier), NumberExpr.uniformInt(4, 8))),
                        new ItemStack(elixir), NumberExpr.constant(1), 3, 1, 0.05f))
                .toList();
    }

    @NotNull
    private static WizardTrade potionSell() {
        Map<NumberExpr, Integer> surcharges = new LinkedHashMap<>();

        for (Potion potion : BuiltInRegistries.POTION) {
            if (!potion.getEffects().isEmpty()) {
                MobEffectInstance effect = potion.getEffects().get(0);
                NumberExpr surcharge = NumberExpr.add(
                        NumberExpr.mul(NumberExpr.constant(effect.getAmplifier()), NumberExpr.uniformInt(4, 6)),
                        NumberExpr.constant(effect.getDuration() / 1200)
                );

                surcharges.merge(surcharge, 1, Integer::sum);
            }
        }

        NumberExpr cost = NumberExpr.uniformInt(12, 16);

        if (!surcharges.isEmpty()) {
            cost = NumberExpr.add(cost, NumberExpr.weighted(surcharges.entrySet().stream()
                    .map((e) -> new NumberExpr.WeightedEntry(e.getValue(), e.getKey()))
                    .toList()));
        }

        return WizardTrade.of(emeralds(), cost, new ItemStack(Items.POTION), NumberExpr.constant(1), 3, 1, 0.05f)
                .withResultTooltip((ignoredUtils) -> TooltipBuilder.keyOnly(IronsSpellbooksLang.Functions.RANDOM_POTION).build());
    }

    @NotNull
    private static WizardTrade randomScroll(SchoolType school, float minQuality, float maxQuality) {
        return SpellScrollTrade.of(new SpellFilter(school), minQuality, maxQuality, emeralds(), stack(ItemRegistry.SCROLL), 1, 5, 0.05f);
    }

    @NotNull
    private static WizardTrade furledMap(int cost, String destination, String translation) {
        ItemStack map = FurledMapItem.of(ResourceLocation.fromNamespaceAndPath(IronsSpellbooksLang.MOD_ID, destination), FurledMapItem.OVERWORLD, Component.translatable(translation));

        return WizardTrade.of(emeralds(), NumberExpr.constant(cost), map, NumberExpr.constant(1), 1, 5, 10.0f);
    }

    @NotNull
    private static WizardTrade simpleBuy(int maxUses, ItemStack buy, int minEmeralds, int maxEmeralds) {
        return WizardTrade.of(buy, NumberExpr.constant(buy.getCount()), emeralds(), NumberExpr.uniformInt(minEmeralds, maxEmeralds), maxUses, 0, 0.05f);
    }

    @NotNull
    private static WizardTrade simpleSell(int maxUses, ItemStack sell, int minEmeralds, int maxEmeralds) {
        return WizardTrade.of(emeralds(), NumberExpr.uniformInt(minEmeralds, maxEmeralds), sell, NumberExpr.constant(sell.getCount()), maxUses, 0, 0.05f);
    }

    @NotNull
    private static List<Item> elixirs(boolean greater) {
        if (greater) {
            return greaterElixirs();
        }

        return List.of(ItemRegistry.EVASION_ELIXIR.get(), ItemRegistry.OAKSKIN_ELIXIR.get(), ItemRegistry.INVISIBILITY_ELIXIR.get());
    }

    @NotNull
    private static List<Item> greaterElixirs() {
        return List.of(ItemRegistry.GREATER_EVASION_ELIXIR.get(), ItemRegistry.GREATER_OAKSKIN_ELIXIR.get(),
                ItemRegistry.GREATER_INVISIBILITY_ELIXIR.get(), ItemRegistry.GREATER_HEALING_POTION.get());
    }

    @NotNull
    private static List<MerchantOffer> fillerOffers() {
        try {
            Field field = ApothecaristEntity.class.getDeclaredField("fillerOffers");

            field.setAccessible(true);
            //noinspection unchecked
            return (List<MerchantOffer>) field.get(null);
        } catch (Throwable e) {
            LOGGER.warn("Failed to read Apothecarist filler offers: {}", e.getMessage(), e);
            return List.of();
        }
    }

    @NotNull
    private static ItemStack fireworkStack() {
        ItemStack rocket = new ItemStack(Items.FIREWORK_ROCKET, 5);
        FireworkExplosion explosion = new FireworkExplosion(FireworkExplosion.Shape.BURST,
                IntList.of(11743535, 15435844, 14602026), IntList.of(), true, true);

        rocket.set(DataComponents.FIREWORKS, new Fireworks(3, List.of(explosion)));

        return rocket;
    }

    @NotNull
    private static ItemStack potion(Holder<Potion> potion) {
        ItemStack stack = new ItemStack(Items.POTION);

        stack.set(DataComponents.POTION_CONTENTS, new PotionContents(potion));

        return stack;
    }

    @NotNull
    private static InkItem ink(Holder<Item> item) {
        return (InkItem) item.value();
    }

    @NotNull
    private static ItemStack emeralds() {
        return new ItemStack(Items.EMERALD);
    }

    @NotNull
    private static ItemStack stack(Holder<Item> item) {
        return new ItemStack(item.value());
    }

    @NotNull
    private static VillagerTrades.ItemListing[] listings(WizardTrade... trades) {
        return trades;
    }
}

package com.yanny.alicompat.compat.occultism;

import com.klikli_dev.occultism.common.entity.spirit.wonderingtrader.WonderingTrades;
import com.klikli_dev.occultism.loot.AddItemModifier;
import com.klikli_dev.occultism.registry.OccultismFoods;
import com.yanny.aci.language.ITooltipKey;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerRegistry;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.TradeLevel;
import com.yanny.ali.language.Lang;
import com.yanny.ali.plugin.glm.IGlobalLootModifierPlugin;
import com.yanny.alicompat.IGlmModCompat;
import com.yanny.alicompat.accessor.GlmAccessorUtils;
import com.yanny.alicompat.accessor.ReflectionUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.trading.TradeSet;
import net.minecraft.world.item.trading.TradeSets;
import net.minecraft.world.item.trading.VillagerTrade;
import net.minecraft.world.level.storage.loot.providers.number.ints.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProvider;
import net.minecraft.world.level.storage.loot.providers.number.ints.UniformGenerator;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class OccultismCompat implements IGlmModCompat {
    private static final Identifier WONDERING_TRADER = Identifier.fromNamespaceAndPath(OccultismLang.MOD_ID, "wondering_trader");

    @NotNull
    @Override
    public String targetModId() {
        return OccultismLang.MOD_ID;
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        registry.registerConsumeEffectTooltip(OccultismFoods.DamageItemConsumeEffect.class, OccultismCompat::getDamageItemTooltip);

        registry.registerTradeLevels(WONDERING_TRADER, OccultismCompat::getWonderingTraderLevels);
    }

    @Override
    public void registerGlobalLootModifier(IGlobalLootModifierPlugin.IRegistry registry) {
        GlmAccessorUtils.registerGlobalLootModifier(registry, AddItemModifier.class, AddItemModifierAccessor.class);
    }

    @NotNull
    private static TooltipBuilder getDamageItemTooltip(IServerUtils utils, OccultismFoods.DamageItemConsumeEffect effect) {
        return TooltipBuilder.array((b) -> b.add(utils.getValueTooltip(utils, effect.amount()).build(Lang.Value.AMOUNT)), OccultismLang.ConsumeEffects.DAMAGE_ITEM);
    }

    @NotNull
    private static Int2ObjectMap<TradeLevel> getWonderingTraderLevels(IServerUtils utils) {
        HolderLookup.RegistryLookup<TradeSet> lookup = utils.lookupProvider().lookup(Registries.TRADE_SET).orElseThrow();
        Int2ObjectMap<TradeLevel> levels = new Int2ObjectOpenHashMap<>();

        putGroup(levels, 1, WonderingTrades.HINT, 1, 1, 1.0F, OccultismLang.Value.WITHOUT_THIRD_EYE);
        putRegistrySet(levels, lookup, 2, TradeSets.WANDERING_TRADER_BUYING);
        putRegistrySet(levels, lookup, 3, TradeSets.WANDERING_TRADER_UNCOMMON);
        putRegistrySet(levels, lookup, 4, TradeSets.WANDERING_TRADER_COMMON);
        putGroup(levels, 5, WonderingTrades.BOOK, 1, 1, 1.0F, OccultismLang.Value.WITH_THIRD_EYE);
        putGroup(levels, 6, WonderingTrades.PARAPHERNALIA, 1, 3, 1.0F, OccultismLang.Value.WITH_THIRD_EYE);
        putGroup(levels, 7, WonderingTrades.MATERIAL, 1, 2, 1.0F, OccultismLang.Value.WITH_THIRD_EYE);
        putGroup(levels, 8, WonderingTrades.INVENTORY, 1, 1, 1.0F, OccultismLang.Value.WITH_THIRD_EYE);
        putGroup(levels, 9, WonderingTrades.STORAGE, 1, 3, 1.0F, OccultismLang.Value.WITH_THIRD_EYE);
        putGroup(levels, 10, WonderingTrades.UTILITY, 1, 1, 1.0F, OccultismLang.Value.WITH_THIRD_EYE);
        putGroup(levels, 11, WonderingTrades.INFUSED, 1, 2, 0.8F, OccultismLang.Value.WITH_THIRD_EYE);
        putGroup(levels, 12, WonderingTrades.FAMILIAR, 1, 1, 0.5F, OccultismLang.Value.WITH_THIRD_EYE);
        putGroup(levels, 13, WonderingTrades.DYE, 1, 1, 0.25F, OccultismLang.Value.WITH_THIRD_EYE);

        return levels;
    }

    private static void putGroup(Int2ObjectMap<TradeLevel> levels, int level, byte group, int min, int max, float chance, ITooltipKey condition) {
        WonderingTrades.ItemListing[] listings = WonderingTrades.WONDERING_TRADES.get(group);

        if (listings == null) {
            return;
        }

        List<Holder<VillagerTrade>> trades = Arrays.stream(listings)
                .filter(WonderingTrades.ItemTrade.class::isInstance)
                .map((listing) -> ReflectionUtils.copyClassData(ItemTradeAccessor.class, listing, WonderingTrades.ItemTrade.class).getTrade())
                .map(Holder::direct)
                .toList();

        if (!trades.isEmpty()) {
            TradeSet tradeSet = new TradeSet(HolderSet.direct(trades), Holder.direct(getAmount(min, max)), false, Optional.empty());

            levels.put(level, new TradeLevel(tradeSet, chance, TooltipBuilder.keyOnly(condition).build()));
        }
    }

    private static void putRegistrySet(Int2ObjectMap<TradeLevel> levels, HolderLookup.RegistryLookup<TradeSet> lookup, int level, ResourceKey<TradeSet> tradeSet) {
        lookup.get(tradeSet).ifPresent((holder) -> levels.put(level,
                new TradeLevel(holder.value(), 1.0F, TooltipBuilder.keyOnly(OccultismLang.Value.WITHOUT_THIRD_EYE).build())));
    }

    @NotNull
    private static ContextIntProvider getAmount(int min, int max) {
        if (min == max) {
            return new ConstantValue(min);
        }

        return new UniformGenerator(Holder.direct(new ConstantValue(min)), Holder.direct(new ConstantValue(max)));
    }
}

package com.yanny.alicompat.compat.occultism;

import com.klikli_dev.occultism.common.entity.spirit.wonderingtrader.WonderingTrades;
import com.klikli_dev.occultism.loot.AddItemModifier;
import com.klikli_dev.occultism.registry.OccultismFoods;
import com.yanny.aci.CommonLogUtils;
import com.yanny.aci.api.RangeValue;
import com.yanny.aci.language.ITooltipKey;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerRegistry;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.TradeLevel;
import com.yanny.ali.api.TradeLevelInfo;
import com.yanny.ali.language.Lang;
import com.yanny.ali.plugin.glm.IGlobalLootModifierPlugin;
import com.yanny.alicompat.IGlmModCompat;
import com.yanny.alicompat.Utils;
import com.yanny.alicompat.accessor.GlmAccessorUtils;
import com.yanny.alicompat.accessor.ReflectionUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.trading.TradeSets;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class OccultismCompat implements IGlmModCompat {
    private static final Logger LOGGER = CommonLogUtils.getLogger(Utils.MOD_ID);
    private static final Identifier WONDERING_TRADER = Identifier.fromNamespaceAndPath(OccultismLang.MOD_ID, "wondering_trader");

    @NotNull
    @Override
    public String targetModId() {
        return OccultismLang.MOD_ID;
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        registry.registerConsumeEffectTooltip(OccultismFoods.DamageItemConsumeEffect.class, OccultismCompat::getDamageItemTooltip);
        registry.registerTrades(WONDERING_TRADER, BuiltInRegistries.ENTITY_TYPE.getValue(WONDERING_TRADER), OccultismCompat::getWonderingTraderLevels);
    }

    @Override
    public void registerGlobalLootModifier(IGlobalLootModifierPlugin.IRegistry registry) {
        GlmAccessorUtils.registerGlobalLootModifier(registry, AddItemModifier.class, AddItemModifierAccessor.class);
    }

    // WonderingTraderEntity#updateTrades and #updateOtherTrades - keyed as levels here, the vanilla sets in between
    @NotNull
    private static Int2ObjectMap<TradeLevel> getWonderingTraderLevels() {
        Int2ObjectMap<TradeLevel> levels = new Int2ObjectOpenHashMap<>();

        levels.put(1, getLevel(WonderingTrades.HINT, new TradeLevelInfo(new RangeValue(1)), OccultismLang.Value.WITHOUT_THIRD_EYE));
        levels.put(2, new TradeLevel.OfSet(TradeSets.WANDERING_TRADER_BUYING));
        levels.put(3, new TradeLevel.OfSet(TradeSets.WANDERING_TRADER_UNCOMMON));
        levels.put(4, new TradeLevel.OfSet(TradeSets.WANDERING_TRADER_COMMON));
        levels.put(5, getLevel(WonderingTrades.BOOK, new TradeLevelInfo(new RangeValue(1)), OccultismLang.Value.WITH_THIRD_EYE));
        levels.put(6, getLevel(WonderingTrades.PARAPHERNALIA, new TradeLevelInfo(new RangeValue(1, 3)), OccultismLang.Value.WITH_THIRD_EYE));
        levels.put(7, getLevel(WonderingTrades.MATERIAL, new TradeLevelInfo(new RangeValue(1, 2)), OccultismLang.Value.WITH_THIRD_EYE));
        levels.put(8, getLevel(WonderingTrades.INVENTORY, new TradeLevelInfo(new RangeValue(1)), OccultismLang.Value.WITH_THIRD_EYE));
        levels.put(9, getLevel(WonderingTrades.STORAGE, new TradeLevelInfo(new RangeValue(1, 3)), OccultismLang.Value.WITH_THIRD_EYE));
        levels.put(10, getLevel(WonderingTrades.UTILITY, new TradeLevelInfo(new RangeValue(1)), OccultismLang.Value.WITH_THIRD_EYE));
        levels.put(11, getLevel(WonderingTrades.INFUSED, new TradeLevelInfo(new RangeValue(1, 2), 0.8f), OccultismLang.Value.WITH_THIRD_EYE));
        levels.put(12, getLevel(WonderingTrades.FAMILIAR, new TradeLevelInfo(new RangeValue(1), 0.5f), OccultismLang.Value.WITH_THIRD_EYE));
        levels.put(13, getLevel(WonderingTrades.DYE, new TradeLevelInfo(new RangeValue(1), 0.25f), OccultismLang.Value.WITH_THIRD_EYE));
        return levels;
    }

    @NotNull
    private static TradeLevel getLevel(byte group, TradeLevelInfo levelInfo, ITooltipKey condition) {
        return new TradeLevel.OfTrades(levelInfo, (utils) -> getTrades(utils, group, condition));
    }

    @NotNull
    private static List<IDataNode> getTrades(IServerUtils utils, byte group, ITooltipKey condition) {
        WonderingTrades.ItemListing[] listings = WonderingTrades.WONDERING_TRADES.get(group);
        TooltipNode conditionNode = TooltipBuilder.keyOnly(condition).build();
        List<IDataNode> trades = new ArrayList<>();

        if (listings != null) {
            for (WonderingTrades.ItemListing listing : listings) {
                if (listing instanceof WonderingTrades.ItemTrade trade) {
                    trades.add(ReflectionUtils.copyClassData(ItemTradeAccessor.class, trade).getNode(utils, conditionNode));
                } else {
                    LOGGER.warn("Unsupported Occultism wondering trader listing {}", listing.getClass().getName());
                }
            }
        }

        return trades;
    }

    @NotNull
    private static TooltipBuilder getDamageItemTooltip(IServerUtils utils, OccultismFoods.DamageItemConsumeEffect effect) {
        return TooltipBuilder.array((b) -> b.add(utils.getValueTooltip(utils, effect.amount()).build(Lang.Value.AMOUNT)), OccultismLang.ConsumeEffects.DAMAGE_ITEM);
    }
}

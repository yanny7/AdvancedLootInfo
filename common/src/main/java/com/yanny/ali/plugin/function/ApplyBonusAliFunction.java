package com.yanny.ali.plugin.function;

import com.mojang.serialization.JsonOps;
import com.yanny.ali.api.IContext;
import com.yanny.ali.api.RangeValue;
import com.yanny.ali.mixin.MixinApplyBonusCount;
import com.yanny.ali.plugin.FunctionTooltipUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

import java.util.List;

public class ApplyBonusAliFunction extends LootConditionalAliFunction {
    public final Holder<Enchantment> enchantment;
    public final ApplyBonusCount.Formula formula;

    public ApplyBonusAliFunction(IContext context, LootItemFunction function) {
        super(context, function);
        enchantment = ((MixinApplyBonusCount) function).getEnchantment();
        formula = ((MixinApplyBonusCount) function).getFormula();
    }

    public ApplyBonusAliFunction(IContext context, FriendlyByteBuf buf) {
        super(context, buf);
        enchantment = BuiltInRegistries.ENCHANTMENT.getHolderOrThrow(buf.readResourceKey(Registries.ENCHANTMENT));
        formula = MixinApplyBonusCount.invokeFormulaCodec().codec().decode(JsonOps.INSTANCE, buf.readJsonWithCodec(ExtraCodecs.JSON)).result().orElseThrow().getFirst();
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        super.encode(context, buf);
        buf.writeResourceKey(enchantment.unwrap().orThrow());
        buf.writeJsonWithCodec(ExtraCodecs.JSON, MixinApplyBonusCount.invokeFormulaCodec().codec().encodeStart(JsonOps.INSTANCE, formula).result().orElseThrow());
    }

    @Override
    public List<Component> getTooltip(int pad) {
        return FunctionTooltipUtils.getApplyBonusTooltip(pad, BuiltInRegistries.ENCHANTMENT.getOptional(enchantment).orElseThrow(), formula);
    }

    public void calculateCount(RangeValue count, int level) {
        if (formula.getType() == ApplyBonusCount.BinomialWithBonusCount.TYPE) {
            count.addMax(((MixinApplyBonusCount.BinomialWithBonusCount) formula).getExtraRounds() + level);
        } else if (formula.getType() == ApplyBonusCount.UniformBonusCount.TYPE) {
            if (level > 0) {
                count.addMax(((MixinApplyBonusCount.UniformBonusCount) formula).getBonusMultiplier() * level);
            }
        } else if (formula.getType() == ApplyBonusCount.OreDrops.TYPE) {
            if (level > 0) {
                count.multiplyMax(level + 1);
            }
        }
    }
}

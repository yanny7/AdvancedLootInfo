package com.yanny.ali.plugin.function;

import com.mojang.serialization.JsonOps;
import com.yanny.ali.api.IContext;
import com.yanny.ali.api.RangeValue;
import com.yanny.ali.mixin.MixinApplyBonusCount;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

import static com.yanny.ali.plugin.TooltipUtils.pad;
import static com.yanny.ali.plugin.TooltipUtils.translatable;

public class ApplyBonusFunction extends LootConditionalFunction {
    public final Holder<Enchantment> enchantment;
    public final ApplyBonusCount.Formula formula;

    public ApplyBonusFunction(IContext context, LootItemFunction function) {
        super(context, function);
        enchantment = ((MixinApplyBonusCount) function).getEnchantment();
        formula = ((MixinApplyBonusCount) function).getFormula();
    }

    public ApplyBonusFunction(IContext context, FriendlyByteBuf buf) {
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
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.apply_bonus")));
        components.add(pad(pad + 1, translatable("ali.property.function.apply_bonus.enchantment", enchantment.value().getDescriptionId())));
        components.add(pad(pad + 1, translatable("ali.property.function.apply_bonus.formula", formula.getType())));
        components.addAll(getFormulaTooltip(pad + 2, formula));

        return components;
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

    @NotNull
    private static List<Component> getFormulaTooltip(int pad, ApplyBonusCount.Formula formula) {
        List<Component> components = new LinkedList<>();

        if (formula.getType() == ApplyBonusCount.BinomialWithBonusCount.TYPE) {
            components.add(pad(pad, translatable("ali.property.function.apply_bonus.formula.binomial.extra_rounds", ((MixinApplyBonusCount.BinomialWithBonusCount) formula).getExtraRounds())));
            components.add(pad(pad, translatable("ali.property.function.apply_bonus.formula.binomial.probability", ((MixinApplyBonusCount.BinomialWithBonusCount) formula).getProbability())));
        } else if (formula.getType() == ApplyBonusCount.UniformBonusCount.TYPE) {
            components.add(pad(pad, translatable("ali.property.function.apply_bonus.formula.uniform.bonus_multiplier", ((MixinApplyBonusCount.UniformBonusCount) formula).getBonusMultiplier())));

        }

        return components;
    }
}

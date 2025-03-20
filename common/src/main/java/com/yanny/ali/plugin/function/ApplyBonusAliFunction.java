package com.yanny.ali.plugin.function;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.yanny.ali.api.IContext;
import com.yanny.ali.api.RangeValue;
import com.yanny.ali.mixin.MixinApplyBonusCount;
import com.yanny.ali.plugin.FunctionTooltipUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

import java.util.List;

public class ApplyBonusAliFunction extends LootConditionalAliFunction {
    public final ResourceLocation enchantment;
    public final ApplyBonusCount.Formula formula;

    public ApplyBonusAliFunction(IContext context, LootItemFunction function) {
        super(context, function);
        enchantment = BuiltInRegistries.ENCHANTMENT.getKey(((MixinApplyBonusCount) function).getEnchantment());
        formula = ((MixinApplyBonusCount) function).getFormula();
    }

    public ApplyBonusAliFunction(IContext context, FriendlyByteBuf buf) {
        super(context, buf);
        enchantment = buf.readResourceLocation();

        ResourceLocation formulaId = buf.readResourceLocation();
        JsonObject parameters = buf.readJsonWithCodec(ExtraCodecs.JSON).getAsJsonObject();
        ApplyBonusCount.FormulaDeserializer formulaDeserializer = MixinApplyBonusCount.invokeFormulas().get(formulaId);

        if (formulaDeserializer == null) {
            throw new JsonParseException("Invalid formula id: " + formulaId);
        } else {
            formula = formulaDeserializer.deserialize(parameters, null);
        }
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        super.encode(context, buf);
        buf.writeResourceLocation(enchantment);
        buf.writeResourceLocation(formula.getType());

        JsonObject parameters = new JsonObject();

        formula.serializeParams(parameters, null);
        buf.writeJsonWithCodec(ExtraCodecs.JSON, parameters);
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

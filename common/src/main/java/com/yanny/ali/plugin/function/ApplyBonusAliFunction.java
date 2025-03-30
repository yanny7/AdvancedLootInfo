package com.yanny.ali.plugin.function;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.yanny.ali.api.IContext;
import com.yanny.ali.api.RangeValue;
import com.yanny.ali.mixin.MixinApplyBonusCount;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

import java.util.Objects;

public class ApplyBonusAliFunction extends LootConditionalAliFunction {
    public final Enchantment enchantment;
    public final ApplyBonusCount.Formula formula;

    public ApplyBonusAliFunction(IContext context, LootItemFunction function) {
        super(context, function);
        enchantment = ((MixinApplyBonusCount) function).getEnchantment();
        formula = ((MixinApplyBonusCount) function).getFormula();
    }

    public ApplyBonusAliFunction(IContext context, FriendlyByteBuf buf) {
        super(context, buf);
        enchantment = BuiltInRegistries.ENCHANTMENT.get(buf.readResourceLocation());

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
        buf.writeResourceLocation(Objects.requireNonNull(BuiltInRegistries.ENCHANTMENT.getKey(enchantment)));
        buf.writeResourceLocation(formula.getType());

        JsonObject parameters = new JsonObject();

        formula.serializeParams(parameters, null);
        buf.writeJsonWithCodec(ExtraCodecs.JSON, parameters);
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

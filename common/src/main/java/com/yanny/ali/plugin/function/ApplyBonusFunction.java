package com.yanny.ali.plugin.function;

import com.yanny.ali.api.IContext;
import com.yanny.ali.api.RangeValue;
import com.yanny.ali.mixin.MixinApplyBonusCount;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public class ApplyBonusFunction extends LootConditionalFunction {
    public final Holder<Enchantment> enchantment;
    public final Formula formula;

    public ApplyBonusFunction(IContext context, LootItemFunction function) {
        super(context, function);
        enchantment = ((MixinApplyBonusCount) function).getEnchantment();
        formula = Formula.of(((MixinApplyBonusCount) function).getFormula());
    }

    public ApplyBonusFunction(IContext context, FriendlyByteBuf buf) {
        super(context, buf);
        enchantment = BuiltInRegistries.ENCHANTMENT.asLookup().get(buf.readResourceKey(BuiltInRegistries.ENCHANTMENT.key())).get();
        formula = Formula.decode(buf);
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        super.encode(context, buf);
        buf.writeResourceLocation(BuiltInRegistries.ENCHANTMENT.getKey(enchantment.value()));
        Formula.encode(buf, formula);
    }

    @Override
    public List<Component> getTooltip(int pad) {
        return List.of();
    }

    public enum FormulaType {
        ORE,
        BINOMIAL,
        UNIFORM,
        UNKNOWN
        ;

        public static FormulaType of(ResourceLocation type) {
            if (type.equals(ApplyBonusCount.OreDrops.TYPE.id())) {
                return ORE;
            } else if (type.equals(ApplyBonusCount.BinomialWithBonusCount.TYPE.id())) {
                return BINOMIAL;
            } else if (type.equals(ApplyBonusCount.UniformBonusCount.TYPE.id())) {
                return UNIFORM;
            } else {
                return UNKNOWN;
            }
        }
    }

    private static final Map<FormulaType, Function<ApplyBonusCount.Formula, Formula>> FORMULA_MAP = new HashMap<>();
    private static final Map<FormulaType, BiFunction<FormulaType, FriendlyByteBuf, Formula>> FORMULA_DECODE_MAP = new HashMap<>();

    static {
        FORMULA_MAP.put(FormulaType.ORE, OreGenFormula::new);
        FORMULA_MAP.put(FormulaType.BINOMIAL, BinomialFormula::new);
        FORMULA_MAP.put(FormulaType.UNIFORM, UniformFormula::new);
        FORMULA_MAP.put(FormulaType.UNKNOWN, UnknownFormula::new);

        FORMULA_DECODE_MAP.put(FormulaType.ORE, OreGenFormula::new);
        FORMULA_DECODE_MAP.put(FormulaType.BINOMIAL, BinomialFormula::new);
        FORMULA_DECODE_MAP.put(FormulaType.UNIFORM, UniformFormula::new);
        FORMULA_DECODE_MAP.put(FormulaType.UNKNOWN, UnknownFormula::new);
    }

    public static abstract class Formula {
        public FormulaType type;

        public Formula(FormulaType type) {
            this.type = type;
        }

        public abstract void calculateCount(RangeValue count, int level);

        public abstract void encode(FriendlyByteBuf buf);

        public static Formula of(ApplyBonusCount.Formula formula) {
            return FORMULA_MAP.get(FormulaType.of(formula.getType().id())).apply(formula);
        }

        public static Formula decode(FriendlyByteBuf buf) {
            FormulaType type = buf.readEnum(FormulaType.class);
            return FORMULA_DECODE_MAP.get(type).apply(type, buf);
        }

        public static void encode(FriendlyByteBuf buf, Formula formula) {
            buf.writeEnum(formula.type);
            formula.encode(buf);
        }
    }

    public static class OreGenFormula extends Formula {
        public OreGenFormula(ApplyBonusCount.Formula formula) {
            super(FormulaType.of(formula.getType().id()));
        }

        public OreGenFormula(FormulaType type, FriendlyByteBuf buf) {
            super(type);
        }

        @Override
        public void calculateCount(RangeValue count, int level) {
            if (level > 0) {
                count.multiplyMax(level + 1);
            }
        }

        @Override
        public void encode(FriendlyByteBuf buf) {
        }
    }

    public static class BinomialFormula extends Formula {
        private final int extraRounds;

        public BinomialFormula(ApplyBonusCount.Formula formula) {
            super(FormulaType.of(formula.getType().id()));
            extraRounds = ((MixinApplyBonusCount.BinomialWithBonusCount) formula).getExtraRounds();
        }

        public BinomialFormula(FormulaType type, FriendlyByteBuf buf) {
            super(type);
            extraRounds = buf.readInt();
        }

        @Override
        public void calculateCount(RangeValue count, int level) {
            count.addMax(extraRounds + level);
        }

        @Override
        public void encode(FriendlyByteBuf buf) {
            buf.writeInt(extraRounds);
        }
    }

    public static class UniformFormula extends Formula {
        private final int bonusMultiplier;

        public UniformFormula(ApplyBonusCount.Formula formula) {
            super(FormulaType.of(formula.getType().id()));
            bonusMultiplier = ((MixinApplyBonusCount.UniformBonusCount) formula).getBonusMultiplier();
        }

        public UniformFormula(FormulaType type, FriendlyByteBuf buf) {
            super(type);
            bonusMultiplier = buf.readInt();
        }

        @Override
        public void calculateCount(RangeValue count, int level) {
            if (level > 0) {
                count.addMax(bonusMultiplier * level);
            }
        }

        @Override
        public void encode(FriendlyByteBuf buf) {
            buf.writeInt(bonusMultiplier);
        }
    }

    public static class UnknownFormula extends Formula {
        public UnknownFormula(ApplyBonusCount.Formula formula) {
            super(FormulaType.of(formula.getType().id()));
        }

        public UnknownFormula(FormulaType type, FriendlyByteBuf buf) {
            super(type);
        }

        @Override
        public void calculateCount(RangeValue count, int level) {

        }

        @Override
        public void encode(FriendlyByteBuf buf) {
        }
    }
}

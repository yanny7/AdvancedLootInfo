package com.yanny.alicompat.compat.villagerconfig;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.IFunctionTooltip;
import com.yanny.alicompat.accessor.IItemStackModifier;
import me.drex.villagerconfig.common.util.loot.function.EnchantRandomlyLootFunction;
import net.minecraft.core.HolderSet;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class EnchantRandomlyLootFunctionAccessor extends BaseAccessor<EnchantRandomlyLootFunction> implements IFunctionTooltip, IItemStackModifier {
    @FieldAccessor
    private Optional<HolderSet<Enchantment>> include;
    @FieldAccessor
    private Optional<HolderSet<Enchantment>> exclude;
    @FieldAccessor
    private int minLevel;
    @FieldAccessor
    private int maxLevel;

    public EnchantRandomlyLootFunctionAccessor(EnchantRandomlyLootFunction parent) {
        super(parent);
    }

    @NotNull
    @Override
    public TooltipBuilder getTooltip(IServerUtils utils) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, include).build(VillagerConfigLang.Branch.INCLUDE));
            b.add(utils.getValueTooltip(utils, exclude).build(VillagerConfigLang.Branch.EXCLUDE));
            b.add(utils.getValueTooltip(utils, minLevel).build(VillagerConfigLang.Value.MIN_LEVEL));
            b.add(utils.getValueTooltip(utils, maxLevel).build(VillagerConfigLang.Value.MAX_LEVEL));
            b.add(utils.getValueTooltip(utils, parent.predicates).build(Lang.Branch.PREDICATES));
        }, VillagerConfigLang.Functions.ENCHANT_RANDOMLY);
    }

    @NotNull
    @Override
    public ItemStack applyItemStackModifier(IServerUtils utils, ItemStack itemStack) {
        if (parent.predicates.isEmpty() && itemStack.is(Items.BOOK)) {
            return Items.ENCHANTED_BOOK.getDefaultInstance();
        }

        return itemStack;
    }
}

package com.yanny.alicompat.compat.apotheosis;

import dev.shadowsoffire.apotheosis.loot.AffixLootEntry;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class ApotheosisUtils {
    private static final ResourceLocation GEM = ResourceLocation.fromNamespaceAndPath(ApotheosisLang.MOD_ID, "gem");

    @NotNull
    public static ItemStack gemStack() {
        return new ItemStack(BuiltInRegistries.ITEM.get(GEM));
    }

    @NotNull
    public static ItemStack firstEntryStack(Collection<DynamicHolder<AffixLootEntry>> entries) {
        return entries.stream().filter(DynamicHolder::isBound).findFirst().map((h) -> h.get().stack().copy()).orElse(ItemStack.EMPTY);
    }

    @NotNull
    public static ItemStack repairMaterial(ItemStack stack) {
        return switch (stack.getItem()) {
            case TieredItem tiered -> firstStack(tiered.getTier().getRepairIngredient());
            case ArmorItem armor -> firstStack(armor.getMaterial().value().repairIngredient().get());
            case ShieldItem ignored when stack.is(Items.SHIELD) -> Items.OAK_PLANKS.getDefaultInstance();
            default -> ItemStack.EMPTY;
        };
    }

    @NotNull
    private static ItemStack firstStack(Ingredient ingredient) {
        ItemStack[] items = ingredient.getItems();

        return items.length > 0 ? items[0] : ItemStack.EMPTY;
    }
}

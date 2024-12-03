package com.yanny.emi_loot_addon.network.condition;

import com.yanny.emi_loot_addon.mixin.MixinItemPredicate;
import com.yanny.emi_loot_addon.mixin.MixinMatchTool;
import com.yanny.emi_loot_addon.network.LootCondition;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class MatchToolCondition extends LootCondition {
    @Nullable public final ResourceLocation tag;
    @Nullable public final Set<ResourceLocation> items;
//    public final MinMaxBounds.Ints count;
//    public final MinMaxBounds.Ints durability;
//    public final EnchantmentPredicate[] enchantments;
//    public final EnchantmentPredicate[] storedEnchantments;
//    @Nullable public final ResourceLocation potion;

    public MatchToolCondition(LootContext lootContext, LootItemCondition condition) {
        super(ConditionType.of(condition.getType()));
        MixinItemPredicate predicate = ((MixinItemPredicate) ((MixinMatchTool) condition).getPredicate());
        TagKey<Item> tag = predicate.getTag();
        Set<Item> items = predicate.getItems();

        if (tag != null) {
            this.tag = tag.location();
        } else {
            this.tag = null;
        }

        if (items != null) {
            this.items = predicate.getItems().stream().map(ForgeRegistries.ITEMS::getKey).collect(Collectors.toSet());
        } else {
            this.items = null;
        }
    }

    public MatchToolCondition(ConditionType type, FriendlyByteBuf buf) {
        super(type);
        items = new HashSet<>();

        if (buf.readBoolean()) {
            tag = buf.readResourceLocation();
        } else {
            tag = null;
        }

        if (buf.readBoolean()) {
            int count = buf.readInt();

            for (int i = 0; i < count; i++) {
                items.add(buf.readResourceLocation());
            }
        }
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(tag != null);
        if (tag != null) {
            buf.writeResourceLocation(tag);
        }

        buf.writeBoolean(items != null);
        if (items != null) {
            buf.writeInt(items.size());
            items.forEach(buf::writeResourceLocation);
        }
    }
}

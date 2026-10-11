package com.yanny.ali.forge.gametest;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.yanny.ali.Utils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid = Utils.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class GameTestLootModifiers {
    @SubscribeEvent
    public static void register(RegisterEvent event) {
        event.register(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, (helper) -> {
            helper.register(Utils.modLoc("gametest_add_item"), AddItem.CODEC.get());
            helper.register(Utils.modLoc("gametest_remove_item"), RemoveItem.CODEC.get());
            helper.register(Utils.modLoc("gametest_unadapted"), Unadapted.CODEC.get());
        });
    }

    public static class AddItem extends LootModifier {
        public static final Supplier<Codec<AddItem>> CODEC = Suppliers.memoize(() -> RecordCodecBuilder.create((inst) -> codecStart(inst)
                .and(ForgeRegistries.ITEMS.getCodec().fieldOf("item").forGetter((m) -> m.item))
                .and(Codec.INT.optionalFieldOf("count", 1).forGetter((m) -> m.count))
                .apply(inst, AddItem::new)));

        public final Item item;
        public final int count;

        public AddItem(LootItemCondition[] conditions, Item item, int count) {
            super(conditions);
            this.item = item;
            this.count = count;
        }

        public LootItemCondition[] getConditions() {
            return conditions;
        }

        @NotNull
        @Override
        protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> loot, LootContext context) {
            loot.add(new ItemStack(item, count));
            return loot;
        }

        @Override
        public Codec<? extends IGlobalLootModifier> codec() {
            return CODEC.get();
        }
    }

    public static class RemoveItem extends LootModifier {
        public static final Supplier<Codec<RemoveItem>> CODEC = Suppliers.memoize(() -> RecordCodecBuilder.create((inst) -> codecStart(inst)
                .and(ForgeRegistries.ITEMS.getCodec().fieldOf("item").forGetter((m) -> m.item))
                .apply(inst, RemoveItem::new)));

        public final Item item;

        public RemoveItem(LootItemCondition[] conditions, Item item) {
            super(conditions);
            this.item = item;
        }

        public LootItemCondition[] getConditions() {
            return conditions;
        }

        @NotNull
        @Override
        protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> loot, LootContext context) {
            loot.removeIf((stack) -> stack.is(item));
            return loot;
        }

        @Override
        public Codec<? extends IGlobalLootModifier> codec() {
            return CODEC.get();
        }
    }

    public static class Unadapted extends LootModifier {
        public static final Supplier<Codec<Unadapted>> CODEC = Suppliers.memoize(() -> RecordCodecBuilder.create((inst) -> codecStart(inst)
                .and(ForgeRegistries.ITEMS.getCodec().fieldOf("item").forGetter((m) -> m.item))
                .apply(inst, Unadapted::new)));

        public final Item item;

        public Unadapted(LootItemCondition[] conditions, Item item) {
            super(conditions);
            this.item = item;
        }

        @NotNull
        @Override
        protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> loot, LootContext context) {
            loot.add(new ItemStack(item));
            return loot;
        }

        @Override
        public Codec<? extends IGlobalLootModifier> codec() {
            return CODEC.get();
        }
    }
}

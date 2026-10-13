package com.yanny.ali.neoforge.gametest;

import com.google.common.base.Suppliers;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.yanny.ali.Utils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Supplier;

@EventBusSubscriber(modid = Utils.MOD_ID)
public class GameTestLootModifiers {
    @SubscribeEvent
    public static void register(RegisterEvent event) {
        event.register(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, (helper) -> {
            helper.register(Utils.modLoc("gametest_add_item"), AddItem.CODEC.get());
            helper.register(Utils.modLoc("gametest_remove_item"), RemoveItem.CODEC.get());
            helper.register(Utils.modLoc("gametest_unadapted"), Unadapted.CODEC.get());
        });
    }

    public static class AddItem extends LootModifier {
        public static final Supplier<MapCodec<AddItem>> CODEC = Suppliers.memoize(() -> RecordCodecBuilder.mapCodec((inst) -> codecStart(inst)
                .and(BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter((m) -> m.item))
                .and(com.mojang.serialization.Codec.INT.optionalFieldOf("count", 1).forGetter((m) -> m.count))
                .apply(inst, AddItem::new)));

        public final Item item;
        public final int count;

        public AddItem(Optional<Holder<LootItemCondition>> condition, int priority, Item item, int count) {
            super(condition, priority);
            this.item = item;
            this.count = count;
        }

        public Optional<Holder<LootItemCondition>> getCondition() {
            return condition;
        }

        @NotNull
        @Override
        protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> loot, LootContext context) {
            loot.add(new ItemStack(item, count));
            return loot;
        }

        @Override
        public MapCodec<? extends IGlobalLootModifier> codec() {
            return CODEC.get();
        }
    }

    public static class RemoveItem extends LootModifier {
        public static final Supplier<MapCodec<RemoveItem>> CODEC = Suppliers.memoize(() -> RecordCodecBuilder.mapCodec((inst) -> codecStart(inst)
                .and(BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter((m) -> m.item))
                .apply(inst, RemoveItem::new)));

        public final Item item;

        public RemoveItem(Optional<Holder<LootItemCondition>> condition, int priority, Item item) {
            super(condition, priority);
            this.item = item;
        }

        public Optional<Holder<LootItemCondition>> getCondition() {
            return condition;
        }

        @NotNull
        @Override
        protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> loot, LootContext context) {
            loot.removeIf((stack) -> stack.is(item));
            return loot;
        }

        @Override
        public MapCodec<? extends IGlobalLootModifier> codec() {
            return CODEC.get();
        }
    }

    public static class Unadapted extends LootModifier {
        public static final Supplier<MapCodec<Unadapted>> CODEC = Suppliers.memoize(() -> RecordCodecBuilder.mapCodec((inst) -> codecStart(inst)
                .and(BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter((m) -> m.item))
                .apply(inst, Unadapted::new)));

        public final Item item;

        public Unadapted(Optional<Holder<LootItemCondition>> condition, int priority, Item item) {
            super(condition, priority);
            this.item = item;
        }

        @NotNull
        @Override
        protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> loot, LootContext context) {
            loot.add(new ItemStack(item));
            return loot;
        }

        @Override
        public MapCodec<? extends IGlobalLootModifier> codec() {
            return CODEC.get();
        }
    }
}

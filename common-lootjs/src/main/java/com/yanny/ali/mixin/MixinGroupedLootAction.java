package com.yanny.ali.mixin;

import com.almostreliable.lootjs.core.filters.ItemFilter;
import com.almostreliable.lootjs.loot.modifier.GroupedLootAction;
import com.almostreliable.lootjs.loot.modifier.LootAction;
import com.yanny.ali.plugin.lootjs.IGroupedLootActionAccessor;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

@Mixin(GroupedLootAction.class)
public abstract class MixinGroupedLootAction implements IGroupedLootActionAccessor {
    @Accessor
    @Override
    public abstract List<LootAction> getActions();

    @Accessor
    @Override
    public abstract ItemFilter getContainsLootFilter();

    @Accessor
    @Override
    public abstract NumberProvider getRolls();

    @Accessor
    @Override
    public abstract List<LootItemFunction> getFunctions();

    @Unique
    private List<LootItemCondition> ali_$injectedConditions;

    @Inject(method = "<init>", at = @At("TAIL"), remap = false)
    private void onConstructor(NumberProvider rolls, List<LootItemCondition> conditions, List<LootItemFunction> functions, Collection<LootAction> actions,
                               @Nullable ItemFilter containsLootFilter, boolean exact, CallbackInfo ci) {
        this.ali_$injectedConditions = conditions;
    }

    @Unique
    @Override
    public List<LootItemCondition> ali_$getInjectedConditions() {
        return this.ali_$injectedConditions;
    }
}

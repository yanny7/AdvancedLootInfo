package com.yanny.alicompat.compat.artifacts;

import artifacts.forge.loot.SmeltOresWithPickaxeHeaterModifier;
import com.yanny.ali.api.ILootModifier;
import com.yanny.ali.api.IOperation;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.plugin.glm.Destination;
import com.yanny.ali.plugin.glm.GlobalLootModifierUtils;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.IDestination;
import com.yanny.alicompat.accessor.IGlobalLootModifierAccessor;
import com.yanny.alicompat.accessor.SmeltingUtils;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.Tags;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

public class SmeltOresWithPickaxeHeaterModifierAccessor extends BaseAccessor<SmeltOresWithPickaxeHeaterModifier> implements IGlobalLootModifierAccessor, IDestination {
    @FieldAccessor
    protected LootItemCondition[] conditions;

    public SmeltOresWithPickaxeHeaterModifierAccessor(SmeltOresWithPickaxeHeaterModifier parent) {
        super(parent);
    }

    @Override
    public Optional<ILootModifier<?>> getLootModifier(IServerUtils utils) {
        return GlobalLootModifierUtils.getLootModifier(utils, parent, Arrays.asList(this.conditions),
                (c) -> Collections.singletonList(new IOperation.ReplaceOperation(
                        SmeltOresWithPickaxeHeaterModifierAccessor::isRawMaterial,
                        (src) -> SmeltingUtils.smeltedNode(utils, c, src))));
    }

    @NotNull
    @Override
    public Destination getDestination(IServerUtils ignoredUtils) {
        return new Destination.Blocks(SmeltOresWithPickaxeHeaterModifierAccessor::isOre, false);
    }

    private static boolean isOre(Block block) {
        return block.defaultBlockState().is(Tags.Blocks.ORES);
    }

    private static boolean isRawMaterial(ItemStack itemStack) {
        return itemStack.is(Tags.Items.RAW_MATERIALS);
    }
}

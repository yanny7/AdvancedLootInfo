package com.yanny.ali.plugin.kubejs;

import com.almostreliable.lootjs.core.LootContextType;
import com.almostreliable.lootjs.core.LootModificationByType;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.mixin.MixinLootModificationByType;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class TypeLootModifier extends LootModifier<ResourceLocation> {
    private final List<LootContextType> types;

    public TypeLootModifier(IServerUtils utils, LootModificationByType byType) {
        super(utils, byType);
        types = ((MixinLootModificationByType) byType).getTypes();
    }

    @Override
    public boolean predicate(ResourceLocation value) {
        String path = value.getPath();

        for (LootContextType type : types) {
            switch (type) {
                case UNKNOWN, ADVANCEMENT_ENTITY, ADVANCEMENT_REWARD -> {
                    return false;
                }
                case BLOCK -> {
                    return path.startsWith("blocks/");
                }
                case ENTITY -> {
                    return path.startsWith("entities/");
                }
                case CHEST -> {
                    return path.startsWith("chests/");
                }
                case FISHING -> {
                    return path.startsWith("gameplay/fishing");
                }
                case GIFT -> {
                    return path.endsWith("_gift");
                }
                case PIGLIN_BARTER -> {
                    return path.endsWith("_bartering");
                }
            }
        }

        return false;
    }

    @Override
    public IType<ResourceLocation> getType() {
        return IType.LOOT_TABLE;
    }
}

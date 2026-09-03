package com.yanny.ali.lootjs;

import com.almostreliable.lootjs.core.LootContextType;
import com.almostreliable.lootjs.core.LootModificationByType;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.lootjs.mixin.MixinLootModificationByType;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

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
            boolean matches = switch (type) {
                case UNKNOWN, ADVANCEMENT_ENTITY, ADVANCEMENT_REWARD -> false;
                case BLOCK -> path.startsWith("blocks/");
                case ENTITY -> path.startsWith("entities/");
                case CHEST -> path.startsWith("chests/");
                case FISHING -> path.startsWith("gameplay/fishing");
                case GIFT -> path.endsWith("_gift");
                case PIGLIN_BARTER -> path.endsWith("_bartering");
            };

            if (matches) {
                return true;
            }
        }

        return false;
    }

    @NotNull
    @Override
    public IType<ResourceLocation> getType() {
        return IType.LOOT_TABLE;
    }
}

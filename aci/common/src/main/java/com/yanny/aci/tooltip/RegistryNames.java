package com.yanny.aci.tooltip;

import com.yanny.aci.CommonLogUtils;
import com.yanny.aci.Utils;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public final class RegistryNames {
    private static final Map<Identifier, Function<Object, Component>> RESOLVERS = new ConcurrentHashMap<>();
    private static final Set<String> LOGGED_FAILURES = ConcurrentHashMap.newKeySet();

    static {
        register(Registries.ITEM, (item) -> item.getDefaultInstance().getHoverName());
        register(Registries.BLOCK, Block::getName);
        register(Registries.ENTITY_TYPE, EntityType::getDescription);
        register(Registries.MOB_EFFECT, MobEffect::getDisplayName);
        register(Registries.ATTRIBUTE, (attribute) -> Component.translatable(attribute.getDescriptionId()));
    }

    private RegistryNames() {}

    @SuppressWarnings("unchecked")
    public static <T> void register(ResourceKey<? extends Registry<T>> registry, Function<T, Component> resolver) {
        RESOLVERS.put(registry.identifier(), (Function<Object, Component>) resolver);
    }

    @NotNull
    static MutableComponent resolve(String encoded) {
        int split = encoded.indexOf(' ');
        String entry = encoded.substring(split + 1);

        try {
            Identifier registryId = Identifier.parse(encoded.substring(1, split));
            Identifier entryId = Identifier.parse(entry);
            Registry<?> registry = BuiltInRegistries.REGISTRY.getValue(registryId);
            Function<Object, Component> resolver = RESOLVERS.get(registryId);

            if (registry != null && resolver != null && registry.containsKey(entryId)) {
                return resolver.apply(registry.getValue(entryId)).copy();
            }
        } catch (Throwable e) {
            if (LOGGED_FAILURES.add(encoded)) {
                CommonLogUtils.getLogger(Utils.MOD_ID).warn("Failed to resolve name of registry entry {}", encoded.substring(1), e);
            }
        }

        return Component.literal(entry);
    }
}

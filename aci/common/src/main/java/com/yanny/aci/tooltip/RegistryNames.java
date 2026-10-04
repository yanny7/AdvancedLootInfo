package com.yanny.aci.tooltip;

import com.yanny.aci.CommonLogUtils;
import com.yanny.aci.Utils;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.LinkedHashSet;
import java.util.List;
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
        register(Registries.POTION, (potion) -> new PotionContents(BuiltInRegistries.POTION.wrapAsHolder(potion)).getName("item.minecraft.potion.effect."));
    }

    private RegistryNames() {}

    @SuppressWarnings("unchecked")
    public static <T> void register(ResourceKey<? extends Registry<T>> registry, Function<T, Component> resolver) {
        RESOLVERS.put(registry.identifier(), (Function<Object, Component>) resolver);
    }

    @NotNull
    public static MutableComponent name(ResourceKey<? extends Registry<?>> registry, Identifier id, String fallback) {
        return translateFirst(entryKeys(registry.identifier(), id), fallback);
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

            return translateFirst(entryKeys(registryId, entryId), entry);
        } catch (Throwable e) {
            if (LOGGED_FAILURES.add(encoded)) {
                CommonLogUtils.getLogger(Utils.MOD_ID).warn("Failed to resolve name of registry entry {}", encoded.substring(1), e);
            }
        }

        return Component.literal(entry);
    }

    @NotNull
    static MutableComponent resolveTag(String encoded) {
        int split = encoded.indexOf(' ');
        String entry = encoded.substring(split + 1);

        try {
            Identifier registryId = Identifier.parse(encoded.substring(1, split));
            Identifier tagId = Identifier.parse(entry);

            return translateFirst(tagKeys(registryId, tagId), entry);
        } catch (Throwable e) {
            if (LOGGED_FAILURES.add(encoded)) {
                CommonLogUtils.getLogger(Utils.MOD_ID).warn("Failed to resolve name of tag {}", encoded.substring(1), e);
            }
        }

        return Component.literal(entry);
    }

    @Unmodifiable
    @NotNull
    static List<String> entryKeys(Identifier registryId, Identifier entryId) {
        String registryPath = registryId.getPath();
        Set<String> prefixes = new LinkedHashSet<>();

        prefixes.add(registryPath.substring(registryPath.lastIndexOf('/') + 1));
        prefixes.addAll(pathShapes(registryPath));

        if (!registryId.getNamespace().equals(Identifier.DEFAULT_NAMESPACE)) {
            for (String prefix : List.copyOf(prefixes)) {
                prefixes.add(registryId.getNamespace() + "." + prefix);
            }
        }

        Set<String> keys = new LinkedHashSet<>();

        for (String prefix : prefixes) {
            for (String path : pathShapes(entryId.getPath())) {
                keys.add(prefix + "." + entryId.getNamespace() + "." + path);
            }
        }

        return List.copyOf(keys);
    }

    @Unmodifiable
    @NotNull
    static List<String> tagKeys(Identifier registryId, Identifier tagId) {
        String registryNamespace = registryId.getNamespace().equals(Identifier.DEFAULT_NAMESPACE) ? "" : registryId.getNamespace() + ".";
        List<String> namespaces = tagId.getNamespace().equals("forge") ? List.of("forge", "c") : List.of(tagId.getNamespace());
        Set<String> keys = new LinkedHashSet<>();

        for (String registryPath : pathShapes(registryId.getPath())) {
            for (String namespace : namespaces) {
                for (String path : pathShapes(tagId.getPath())) {
                    keys.add("tag." + registryNamespace + registryPath + "." + namespace + "." + path);
                }
            }
        }

        for (String namespace : namespaces) {
            for (String path : pathShapes(tagId.getPath())) {
                keys.add("tag." + namespace + "." + path);
            }
        }

        return List.copyOf(keys);
    }

    @NotNull
    private static Set<String> pathShapes(String path) {
        return new LinkedHashSet<>(List.of(path.replace('/', '.'), path));
    }

    @NotNull
    private static MutableComponent translateFirst(List<String> keys, String fallback) {
        Language language = Language.getInstance();

        for (String key : keys) {
            if (language.has(key)) {
                return Component.translatable(key);
            }
        }

        return Component.literal(fallback);
    }
}

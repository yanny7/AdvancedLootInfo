package com.yanny.advanced_loot_info.manager;

import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.yanny.advanced_loot_info.api.*;
import com.yanny.advanced_loot_info.plugin.condition.UnknownCondition;
import com.yanny.advanced_loot_info.plugin.entry.SingletonEntry;
import com.yanny.advanced_loot_info.plugin.entry.UnknownEntry;
import com.yanny.advanced_loot_info.plugin.function.UnknownFunction;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.widget.Bounds;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import static com.yanny.advanced_loot_info.plugin.WidgetUtils.GROUP_WIDGET_WIDTH;
import static com.yanny.advanced_loot_info.plugin.WidgetUtils.VERTICAL_OFFSET;

public class AliRegistry implements ICommonRegistry, ICommonUtils, IClientRegistry, IClientUtils {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ResourceLocation UNKNOWN = new ResourceLocation("unknown");

    private final Map<ResourceLocation, Pair<BiFunction<IContext, LootItemFunction, ILootFunction>, BiFunction<IContext, FriendlyByteBuf, ILootFunction>>> functionMap = new HashMap<>();
    private final Map<ResourceLocation, Pair<BiFunction<IContext, LootItemCondition, ILootCondition>, BiFunction<IContext, FriendlyByteBuf, ILootCondition>>> conditionMap = new HashMap<>();
    private final Map<ResourceLocation, Pair<BiFunction<IContext, LootPoolEntryContainer, ILootEntry>, BiFunction<IContext, FriendlyByteBuf, ILootEntry>>> entryMap = new HashMap<>();
    private final Map<Class<?>, ResourceLocation> functionClassMap = new HashMap<>();
    private final Map<Class<?>, ResourceLocation> conditionClassMap = new HashMap<>();
    private final Map<Class<?>, ResourceLocation> entryClassMap = new HashMap<>();
    private final Map<Class<?>, IWidgetFactory> widgetMap = new HashMap<>();
    private final Map<Class<?>, WidgetDirection> widgetDirectionMap = new HashMap<>();
    private final Map<Class<?>, IBoundsGetter> widgetBoundsMap = new HashMap<>();
    private final Map<ResourceLocation, BiFunction<IContext, NumberProvider, RangeValue>> numberConverterMap = new HashMap<>();

    @Override
    public <T extends ILootFunction> void registerFunction(Class<T> clazz,
                                                           ResourceLocation key,
                                                           BiFunction<IContext, LootItemFunction, ILootFunction> functionEncoder,
                                                           BiFunction<IContext, FriendlyByteBuf, ILootFunction> functionDecoder) {
        functionMap.put(key, new Pair<>(functionEncoder, functionDecoder));
        functionClassMap.put(clazz, key);
    }

    @Override
    public <T extends ILootCondition> void registerCondition(Class<T> clazz,
                                                             ResourceLocation key,
                                                             BiFunction<IContext, LootItemCondition, ILootCondition> conditionEncoder,
                                                             BiFunction<IContext, FriendlyByteBuf, ILootCondition> conditionDecoder) {
        conditionMap.put(key, new Pair<>(conditionEncoder, conditionDecoder));
        conditionClassMap.put(clazz, key);
    }

    @Override
    public <T extends ILootEntry> void registerEntry(Class<T> clazz,
                                                    ResourceLocation key,
                                                    BiFunction<IContext, LootPoolEntryContainer, ILootEntry> entryEncoder,
                                                    BiFunction<IContext, FriendlyByteBuf, ILootEntry> entryDecoder) {
        entryMap.put(key, new Pair<>(entryEncoder, entryDecoder));
        entryClassMap.put(clazz, key);
    }

    @Override
    public <T extends ILootEntry> void registerWidget(Class<T> clazz, WidgetDirection direction, IWidgetFactory factory, IBoundsGetter boundsGetter) {
        widgetMap.put(clazz, factory);
        widgetDirectionMap.put(clazz, direction);
        widgetBoundsMap.put(clazz, boundsGetter);
    }

    @Override
    public void registerNumberProvider(ResourceLocation key, BiFunction<IContext, NumberProvider, RangeValue> converter) {
        numberConverterMap.put(key, converter);
    }

    @Override
    public ILootCondition convertCondition(IContext context, LootItemCondition condition) {
        ResourceLocation key = BuiltInRegistries.LOOT_CONDITION_TYPE.getKey(condition.getType());

        if (key != null) {
            Pair<BiFunction<IContext, LootItemCondition, ILootCondition>, BiFunction<IContext, FriendlyByteBuf, ILootCondition>> pair = conditionMap.get(key);

            if (pair != null) {
                return pair.getFirst().apply(context, condition);
            } else {
                LOGGER.warn("Encode condition {} was not registered", key);
            }
        }

        return new UnknownCondition(context, condition);
    }

    @Override
    public List<ILootCondition> convertConditions(IContext context, LootItemCondition[] conditions) {
        List<ILootCondition> list = new LinkedList<>();

        for (LootItemCondition condition : conditions) {
            list.add(convertCondition(context, condition));
        }

        return list;
    }

    @Override
    public ILootCondition decodeCondition(IContext context, FriendlyByteBuf buf) {
        ResourceLocation key = buf.readResourceLocation();

        if (key != UNKNOWN) {
            Pair<BiFunction<IContext, LootItemCondition, ILootCondition>, BiFunction<IContext, FriendlyByteBuf, ILootCondition>> pair = conditionMap.get(key);

            if (pair != null) {
                return pair.getSecond().apply(context, buf);
            } else {
                LOGGER.warn("Decode condition {} was not registered", key);
            }
        }

        return new UnknownCondition(context, buf);
    }

    @Override
    public List<ILootCondition> decodeConditions(IContext context, FriendlyByteBuf buf) {
        int count = buf.readInt();
        List<ILootCondition> list = new LinkedList<>();

        for (int i = 0; i < count; i++) {
            list.add(decodeCondition(context, buf));
        }

        return list;
    }

    @Override
    public void encodeCondition(IContext context, FriendlyByteBuf buf, ILootCondition condition) {
        ResourceLocation key = conditionClassMap.getOrDefault(condition.getClass(), UNKNOWN);
        buf.writeResourceLocation(key);
        condition.encode(context, buf);
    }

    @Override
    public void encodeConditions(IContext context, FriendlyByteBuf buf, List<ILootCondition> conditions) {
        buf.writeInt(conditions.size());

        for (ILootCondition condition : conditions) {
            encodeCondition(context, buf, condition);
        }
    }

    @Override
    public ILootFunction convertFunction(IContext context, LootItemFunction function) {
        ResourceLocation key = BuiltInRegistries.LOOT_FUNCTION_TYPE.getKey(function.getType());

        if (key != null) {
            Pair<BiFunction<IContext, LootItemFunction, ILootFunction>, BiFunction<IContext, FriendlyByteBuf, ILootFunction>> pair = functionMap.get(key);

            if (pair != null) {
                return pair.getFirst().apply(context, function);
            } else {
                LOGGER.warn("Encode function {} was not registered", key);
            }
        }

        return new UnknownFunction(context, function);
    }

    @Override
    public List<ILootFunction> convertFunctions(IContext context, LootItemFunction[] functions) {
        List<ILootFunction> list = new LinkedList<>();

        for (LootItemFunction function : functions) {
            list.add(convertFunction(context, function));
        }

        return list;
    }

    @Override
    public ILootFunction decodeFunction(IContext context, FriendlyByteBuf buf) {
        ResourceLocation key = buf.readResourceLocation();

        if (key != UNKNOWN) {
            Pair<BiFunction<IContext, LootItemFunction, ILootFunction>, BiFunction<IContext, FriendlyByteBuf, ILootFunction>> pair = functionMap.get(key);

            if (pair != null) {
                return pair.getSecond().apply(context, buf);
            } else {
                LOGGER.warn("Decode function {} was not registered", key);
            }
        }
        
        return new UnknownFunction(context, buf);
    }

    @Override
    public List<ILootFunction> decodeFunctions(IContext context, FriendlyByteBuf buf) {
        int count = buf.readInt();
        List<ILootFunction> list = new LinkedList<>();

        for (int i = 0; i < count; i++) {
            list.add(decodeFunction(context, buf));
        }

        return list;
    }

    @Override
    public void encodeFunction(IContext context, FriendlyByteBuf buf, ILootFunction function) {
        ResourceLocation key = functionClassMap.getOrDefault(function.getClass(), UNKNOWN);
        buf.writeResourceLocation(key);
        function.encode(context, buf);
    }

    @Override
    public void encodeFunctions(IContext context, FriendlyByteBuf buf, List<ILootFunction> functions) {
        buf.writeInt(functions.size());

        for (ILootFunction function : functions) {
            encodeFunction(context, buf, function);
        }
    }

    @Override
    public ILootEntry convertEntry(IContext context, LootPoolEntryContainer entry) {
        ResourceLocation key = BuiltInRegistries.LOOT_POOL_ENTRY_TYPE.getKey(entry.getType());

        if (key != null) {
            Pair<BiFunction<IContext, LootPoolEntryContainer, ILootEntry>, BiFunction<IContext, FriendlyByteBuf, ILootEntry>> pair = entryMap.get(key);

            if (pair != null) {
                return pair.getFirst().apply(context, entry);
            } else {
                LOGGER.warn("Encode entry {} was not registered", key);
            }
        }

        return new UnknownEntry(context, entry);
    }

    @Override
    public List<ILootEntry> convertEntries(IContext context, LootPoolEntryContainer[] entries) {
        List<ILootEntry> list = new LinkedList<>();

        for (LootPoolEntryContainer entry : entries) {
            list.add(convertEntry(context, entry));
        }

        return list;
    }

    @Override
    public ILootEntry decodeEntry(IContext context, FriendlyByteBuf buf) {
        ResourceLocation key = buf.readResourceLocation();

        if (key != UNKNOWN) {
            Pair<BiFunction<IContext, LootPoolEntryContainer, ILootEntry>, BiFunction<IContext, FriendlyByteBuf, ILootEntry>> pair = entryMap.get(key);

            if (pair != null) {
                return pair.getSecond().apply(context, buf);
            } else {
                LOGGER.warn("Decode entry {} was not registered", key);
            }
        }

        return new UnknownEntry(context, buf);
    }

    @Override
    public List<ILootEntry> decodeEntries(IContext context, FriendlyByteBuf buf) {
        int count = buf.readInt();
        List<ILootEntry> list = new LinkedList<>();

        for (int i = 0; i < count; i++) {
            list.add(decodeEntry(context, buf));
        }

        return list;
    }

    @Override
    public void encodeEntry(IContext context, FriendlyByteBuf buf, ILootEntry entry) {
        ResourceLocation key = entryClassMap.getOrDefault(entry.getClass(), UNKNOWN);
        buf.writeResourceLocation(key);
        entry.encode(context, buf);
    }

    @Override
    public void encodeEntries(IContext context, FriendlyByteBuf buf, List<ILootEntry> entries) {
        buf.writeInt(entries.size());

        for (ILootEntry entry : entries) {
            encodeEntry(context, buf, entry);
        }
    }

    @Override
    public RangeValue convertNumber(IContext context, @Nullable NumberProvider numberProvider) {
        try {
            if (numberProvider != null) {
                ResourceLocation key = BuiltInRegistries.LOOT_NUMBER_PROVIDER_TYPE.getKey(numberProvider.getType());

                if (key != null) {
                    BiFunction<IContext, NumberProvider, RangeValue> function = numberConverterMap.get(key);

                    if (function != null) {
                        return function.apply(context, numberProvider);
                    } else {
                        LOGGER.warn("Number converter {} was not registered", key);
                    }
                }

            }

            return new RangeValue(false, true);
        } catch (Exception e) {
            LOGGER.error("Failed to convert number: {}", e.getMessage());
            return new RangeValue();
        }
    }

    @Override
    public Pair<List<EntryWidget>, Bounds> createWidgets(EmiRecipe recipe, IClientUtils utils, List<ILootEntry> entries, int x, int y,
                                                         List<ILootFunction> functions, List<ILootCondition> conditions) {
        int posX = x + GROUP_WIDGET_WIDTH, posY = y;
        int width = 0, height = 0;
        int sumWeight = 0;
        List<EntryWidget> widgets = new LinkedList<>();
        WidgetDirection lastDirection = null;

        for (ILootEntry entry : entries) {
            if (entry instanceof SingletonEntry singletonEntry) {
                sumWeight += singletonEntry.weight;
            }
        }

        for (ILootEntry entry : entries) {
            WidgetDirection direction = PluginManager.REGISTRY.widgetDirectionMap.get(entry.getClass());
            IWidgetFactory widgetFactory = PluginManager.REGISTRY.widgetMap.get(entry.getClass());
            IBoundsGetter bounds = PluginManager.REGISTRY.widgetBoundsMap.get(entry.getClass());

            if (direction != null && widgetFactory != null && bounds != null) {
                if (lastDirection != null && direction != lastDirection && direction == WidgetDirection.VERTICAL) {
                    posX = x + GROUP_WIDGET_WIDTH;
                    posY = y + height + VERTICAL_OFFSET;
                }

                Bounds bound = bounds.apply(utils, entry, posX, posY);

                if (bound.right() > 9 * 18) {
                    posX = x + GROUP_WIDGET_WIDTH;
                    posY += bound.height();
                    bound = bounds.apply(utils, entry, posX, posY);
                }

                EntryWidget widget = widgetFactory.create(recipe, utils, entry, posX, posY, sumWeight, List.copyOf(functions), List.copyOf(conditions));
                width = Math.max(width, bound.right() - x);
                height = Math.max(height, bound.bottom() - y);

                if (lastDirection != null) {
                    if (lastDirection != direction) {
                        posX = x + GROUP_WIDGET_WIDTH;
                        posY += bound.height() + VERTICAL_OFFSET;
                    } else if (direction == WidgetDirection.HORIZONTAL) {
                        posX += bound.width();
                    } else if (direction == WidgetDirection.VERTICAL) {
                        posY += bound.height() + VERTICAL_OFFSET;
                    }
                } else {
                    switch (direction) {
                        case HORIZONTAL -> posX += bound.width();
                        case VERTICAL -> posY += bound.height() + VERTICAL_OFFSET;
                    }
                }

                widgets.add(widget);
                lastDirection = direction;
            }
        }

        return new Pair<>(widgets, new Bounds(x, y, width, height));
    }

    @Override
    public Bounds getBounds(IClientUtils utils, List<ILootEntry> entries, int x, int y) {
        int posX = x + GROUP_WIDGET_WIDTH, posY = y;
        int width = 0, height = 0;
        WidgetDirection lastDirection = null;

        for (ILootEntry entry : entries) {
            WidgetDirection direction = PluginManager.REGISTRY.widgetDirectionMap.get(entry.getClass());
            IWidgetFactory widgetFactory = PluginManager.REGISTRY.widgetMap.get(entry.getClass());
            IBoundsGetter bounds = PluginManager.REGISTRY.widgetBoundsMap.get(entry.getClass());

            if (direction != null && widgetFactory != null && bounds != null) {
                if (lastDirection != null && direction != lastDirection && direction == WidgetDirection.VERTICAL) {
                    posX = x + GROUP_WIDGET_WIDTH;
                    posY = y + height + VERTICAL_OFFSET;
                }

                Bounds bound = bounds.apply(utils, entry, posX, posY);

                if (bound.right() > 9 * 18) {
                    posX = x + GROUP_WIDGET_WIDTH;
                    posY += bound.height();
                    bound = bounds.apply(utils, entry, posX, posY);
                }

                width = Math.max(width, bound.right() - x);
                height = Math.max(height, bound.bottom() - y);

                if (lastDirection != null) {
                    if (lastDirection != direction) {
                        posX = x + GROUP_WIDGET_WIDTH;
                        posY += bound.height() + VERTICAL_OFFSET;
                    } else if (direction == WidgetDirection.HORIZONTAL) {
                        posX += bound.width();
                    } else if (direction == WidgetDirection.VERTICAL) {
                        posY += bound.height() + VERTICAL_OFFSET;
                    }
                } else {
                    switch (direction) {
                        case HORIZONTAL -> posX += bound.width();
                        case VERTICAL -> posY += bound.height() + VERTICAL_OFFSET;
                    }
                }

                lastDirection = direction;
            }
        }

        return new Bounds(x, y, width, height);
    }

    @Nullable
    @Override
    public WidgetDirection getWidgetDirection(ILootEntry entry) {
        return widgetDirectionMap.get(entry.getClass());
    }

    public void printCommonInfo() {
        LOGGER.info("Registered {} loot functions", functionMap.size());
        LOGGER.info("Registered {} loot conditions", conditionMap.size());
        LOGGER.info("Registered {} loot pool entries", entryMap.size());
        LOGGER.info("Registered {} number converters", numberConverterMap.size());
    }

    public void printClientInfo() {
        LOGGER.info("Registered {} widgets", widgetMap.size());
    }
}

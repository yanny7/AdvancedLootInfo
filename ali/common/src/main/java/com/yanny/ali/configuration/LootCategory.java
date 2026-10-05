package com.yanny.ali.configuration;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.yanny.aci.configuration.ConfigCodecs;
import com.yanny.ali.Utils;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public abstract class LootCategory<T> {
    protected static final Codec<Pattern> PATTERN_CODEC = Codec.STRING.comapFlatMap(LootCategory::compile, Pattern::pattern);

    private final Identifier key;
    private final Item icon;
    private final Type type;
    private final boolean hide;
    private final List<Ingredient> catalysts;

    public LootCategory(Identifier key, Item icon, Type type, boolean hide, List<Ingredient> catalysts) {
        this.key = key;
        this.icon = icon;
        this.type = type;
        this.hide = hide;
        this.catalysts = catalysts;
    }

    public abstract boolean validate(T t);

    @NotNull
    protected static Codec<List<Ingredient>> catalysts() {
        return lenientList(Ingredient.CODEC, "catalysts");
    }

    private static DataResult<Pattern> compile(String regex) {
        try {
            return DataResult.success(Pattern.compile(regex));
        } catch (PatternSyntaxException e) {
            return DataResult.error(e::getMessage);
        }
    }

    @NotNull
    protected static <E> Codec<List<E>> lenientList(Codec<E> element, String name) {
        return ConfigCodecs.lenientList(Utils.MOD_ID, element, name);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()){
            return false;
        }

        LootCategory<?> that = (LootCategory<?>) o;
        return Objects.equals(key, that.key) && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, type);
    }

    public Identifier getKey() {
        return key;
    }

    public Item getIcon() {
        return icon;
    }

    public Type getType() {
        return type;
    }

    public boolean isHidden() {
        return hide;
    }

    public List<Ingredient> getCatalysts() {
        return catalysts;
    }

    public enum Type {
        BLOCK,
        ENTITY,
        GAMEPLAY,
        TRADE
    }
}

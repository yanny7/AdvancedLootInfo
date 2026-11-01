package com.yanny.ali.configuration;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.yanny.aci.configuration.ConfigCodecs;
import com.yanny.ali.Utils;

public class CodeAnalysis {
    public static final Codec<CodeAnalysis> CODEC = RecordCodecBuilder.create((instance) ->
        instance.group(
                ConfigCodecs.field(Utils.MOD_ID, Codec.BOOL, "entityLootTables", () -> true).forGetter((c) -> c.entityLootTables),
                ConfigCodecs.field(Utils.MOD_ID, Codec.BOOL, "entityVariants", () -> true).forGetter((c) -> c.entityVariants),
                ConfigCodecs.field(Utils.MOD_ID, Codec.BOOL, "traders", () -> true).forGetter((c) -> c.traders),
                ConfigCodecs.field(Utils.MOD_ID, Codec.BOOL, "tradeListings", () -> true).forGetter((c) -> c.tradeListings)
        ).apply(instance, (entityLootTables, entityVariants, traders, tradeListings) -> {
            CodeAnalysis analysis = new CodeAnalysis();

            analysis.entityLootTables = entityLootTables;
            analysis.entityVariants = entityVariants;
            analysis.traders = traders;
            analysis.tradeListings = tradeListings;
            return analysis;
        })
    );

    public boolean entityLootTables = true;
    public boolean entityVariants = true;
    public boolean traders = true;
    public boolean tradeListings = true;
}

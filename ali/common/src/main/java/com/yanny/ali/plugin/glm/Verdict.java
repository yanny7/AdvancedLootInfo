package com.yanny.ali.plugin.glm;

import org.jetbrains.annotations.NotNull;

public record Verdict(Match match, boolean explained) {
    public static final Verdict NO = new Verdict(Match.NO, false);
    public static final Verdict UNKNOWN = new Verdict(Match.UNKNOWN, false);

    @NotNull
    public static Verdict yes(boolean explained) {
        return new Verdict(Match.YES, explained);
    }
}

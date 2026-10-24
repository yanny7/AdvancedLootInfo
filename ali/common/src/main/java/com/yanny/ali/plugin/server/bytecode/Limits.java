package com.yanny.ali.plugin.server.bytecode;

final class Limits {
    static final int PATHS = 128;
    static final int DECISIONS = 8;
    static final int STEPS = 200_000;
    static final int DEPTH = 12;
    static final int OPTIONS = 1024;
    static final int MAX_RESTARTS = 8;

    private Limits() {}
}

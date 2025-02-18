package com.yanny.ali.network;

import org.jetbrains.annotations.Nullable;

public record DistHolder<Client, Server>(@Nullable Client client, Server server) {}

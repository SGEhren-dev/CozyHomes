package com.ehrengrenlund.cozyhomes.utils;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.ServerCommandSource;
import org.jetbrains.annotations.NotNull;

public class CommandRegistry {
    public static void RegisterCommand(@NotNull LiteralArgumentBuilder<ServerCommandSource> builder) {
        CommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess, environment) -> {
            dispatcher.register(builder);
        }));
    }
}

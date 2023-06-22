package com.ehrengrenlund.cozyhomes.commands;

import com.ehrengrenlund.cozyhomes.data.Home;
import com.ehrengrenlund.cozyhomes.configuration.Configuration;
import com.ehrengrenlund.cozyhomes.utils.CommandRegistry;
import com.ehrengrenlund.cozyhomes.utils.CozyLogger;
import com.ehrengrenlund.cozyhomes.utils.CozyUtils;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static com.ehrengrenlund.cozyhomes.data.PlayerInitializer.HOME_DATA;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class HomeCommand {
    private final Configuration serverConfig;
    private final HashMap<UUID, Long> recentRequests = new HashMap<>();
    private final CozyLogger cozyLogger;

    public HomeCommand(Configuration config, CozyLogger logger) {
        this.serverConfig = config;
        this.cozyLogger = logger;
    }

    public void RegisterCommands() {
        CommandRegistry.RegisterCommand(literal("home")
                .executes(ctx -> initializeHome(ctx, null))
                .then(argument("name", StringArgumentType.greedyString()).suggests(this::getHomeSuggestions)
                        .executes(ctx -> initializeHome(ctx, StringArgumentType.getString(ctx, "name")))));

        CommandRegistry.RegisterCommand(literal("sethome")
                .executes(ctx-> setHome(ctx, null))
                .then(argument("name", StringArgumentType.greedyString())
                        .executes(ctx -> setHome(ctx, StringArgumentType.getString(ctx, "name")))));

        CommandRegistry.RegisterCommand(literal("delhome")
                .executes(ctx -> deleteHome(ctx, null))
                .then(argument("name", StringArgumentType.greedyString())
                        .executes(ctx -> deleteHome(ctx, StringArgumentType.getString(ctx, "name")))));

        CommandRegistry.RegisterCommand(literal("listhomes").executes(this::listHomes));
    }

    private boolean isOnCooldown(ServerPlayerEntity entity) {
        if (recentRequests.containsKey(entity.getUuid())) {
            long diff = Instant.now().getEpochSecond() - recentRequests.get(entity.getUuid());

            if (diff < serverConfig.getCoolDownTime()) {
                entity.sendMessage(Text.translatable("You must wait %s seconds before teleporting again", String.valueOf(serverConfig.getCoolDownTime() - diff)).formatted(Formatting.RED), false);
                return true;
            }
        }

        return false;
    }

    private CompletableFuture<Suggestions> getHomeSuggestions(CommandContext<ServerCommandSource> ctx, SuggestionsBuilder builder) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        String start = builder.getRemainingLowerCase();

        if (player == null)
            return null;

        HOME_DATA.get(player).getHomes().stream()
                .map(Home::GetName)
                .sorted(String::compareToIgnoreCase)
                .filter(v -> v.toLowerCase().startsWith(start))
                .forEach(builder::suggest);

        return builder.buildFuture();
    }

    private int initializeHome(CommandContext<ServerCommandSource> ctx, String name) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayer();

        if (player == null)
            return 1;


        if (name == null)
            name = "main";

        String finalName = name;
        Optional<Home> home = HOME_DATA.get(player).getHomes().stream()
                .filter(nbt -> nbt.GetName().equals(finalName)).findFirst();

        if (home.isEmpty()) {
            ctx.getSource().sendMessage(Text.literal("This home does not exist").formatted(Formatting.RED));
            return 0;
        }

        if (isOnCooldown(player))
            return 1;

        CozyUtils.InitiateTeleport(serverConfig.getShowBossBar(), serverConfig.getStandStillTime(), player, () -> {
            cozyLogger.info(home.get().GetDimId().toString());

            player.teleport(
                    ctx.getSource().getServer().getWorld(World.OVERWORLD),
                    home.get().GetX(), home.get().GetY(), home.get().GetZ(),
                    home.get().GetYaw(), home.get().GetPitch()
            );

            recentRequests.put(player.getUuid(), Instant.now().getEpochSecond());
        });

        return 1;
    }

    private int setHome(CommandContext<ServerCommandSource> ctx, String name) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        if (player == null)
            return 0;

        if (name == null)
            name = "main";

        if (HOME_DATA.get(player).getHomes().size() >= serverConfig.getMaxHomes()) {
            ctx.getSource().sendMessage(Text.literal("Home limit reached!").formatted(Formatting.RED));
            return 1;
        }

        if (HOME_DATA.get(player).addHome(new Home(
                ctx.getSource().getPosition(),
                ctx.getSource().getPlayer().getPitch(),
                ctx.getSource().getPlayer().getYaw(),
                ctx.getSource().getWorld().getRegistryKey().getValue(),
                name
        ))) {
            String finalName = name;
            Optional<Home> home = HOME_DATA.get(player).getHomes()
                    .stream().filter(h -> h.GetName().equals(finalName)).findFirst();

            if (home.isEmpty()) {
                ctx.getSource().sendMessage(Text.literal("Something went wrong adding your home.").formatted(Formatting.RED));
                return 1;
            }

            ctx.getSource().sendMessage(Text.translatable("Home %s added successfully!",
                    Text.literal(name).styled(s -> s.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, home.get().ToText()))
                            .withColor(Formatting.GOLD))).formatted(Formatting.LIGHT_PURPLE));
        } else {
            ctx.getSource().sendMessage(Text.literal("Couldn't add the home, likely already exists.").formatted(Formatting.RED));
        }

        return 1;
    }

    private int deleteHome(CommandContext<ServerCommandSource> ctx, String name) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayer();

        if (player == null)
            return 0;

        if (HOME_DATA.get(player).removeHome(name)) {
            Optional<Home> home = HOME_DATA.get(player).getHomes()
                    .stream().filter(nbt -> nbt.GetName().equals(name)).findFirst();

            if (home.isPresent()) {
                ctx.getSource().sendMessage(Text.literal("Something went wrong removing the home").formatted(Formatting.RED));
                return 1;
            }

            ctx.getSource().sendMessage(Text.translatable("Home %s deleted successfully.",
                    Text.literal(name).formatted(Formatting.GOLD)).formatted(Formatting.LIGHT_PURPLE));
        } else {
            ctx.getSource().sendMessage(Text.literal("Couldn't remove the home.").formatted(Formatting.RED));
        }

        return 1;
    }

    private int listHomes(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        return listHomes(ctx, ctx.getSource().getPlayer());
    }

    private int listHomes(CommandContext<ServerCommandSource> ctx, ServerPlayerEntity player) {
        List<Home> homes = HOME_DATA.get(player).getHomes();
        List<Text> homeList = new ArrayList<>();

        homes.stream().sorted((h1, h2) -> h1.GetName().compareToIgnoreCase(h2.GetName())).forEach(h ->
                homeList.add(Text.literal(h.GetName()).styled(s ->
                        s.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/home" + h.GetName()))
                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                        Text.empty().append(Text.literal("Click to teleport.\n").formatted(Formatting.ITALIC))
                                                .append(h.ToText())))
                                .withColor(Formatting.GOLD))));

        ctx.getSource().sendMessage(Text.translatable("%s/%s:\n", homes.size(), serverConfig.getMaxHomes()).append(CozyUtils.join(homeList, Text.literal(", "))));
        return 1;
    }
}

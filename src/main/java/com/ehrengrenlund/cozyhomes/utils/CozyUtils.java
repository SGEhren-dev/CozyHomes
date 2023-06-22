package com.ehrengrenlund.cozyhomes.utils;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.CommandBossBar;
import net.minecraft.network.packet.s2c.play.ClearTitleS2CPacket;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

public class CozyUtils {
    private static final String BossBarPrefix = "CozyHomes";
    private static CommandBossBar countdownBar = null;
    private static double counter = 0;
    private static Vec3d lastPosition = null;
    private static ServerPlayerEntity playerEntity;

    public static void InitiateTeleport(boolean showCountdownBar, double standStillTime, ServerPlayerEntity player, Runnable counterCallback) {
        MinecraftServer server = player.server;

        counter = standStillTime;
        lastPosition = player.getPos();

        if (showCountdownBar) {
            countdownBar = server.getBossBarManager().add(new Identifier(BossBarPrefix + player.getUuidAsString()), Text.literal("Teleporting, stand still!").formatted(Formatting.AQUA));
            countdownBar.addPlayer(player);
            countdownBar.setColor(BossBar.Color.BLUE);
        }

        player.networkHandler.sendPacket(new TitleFadeS2CPacket(0, 10, 5));

        CommandBossBar finalCountDownBar = countdownBar;

        playerEntity = player;
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (counter == 0) {
                    if (showCountdownBar) {
                        finalCountDownBar.removePlayer(playerEntity);
                        server.getBossBarManager().remove(finalCountDownBar);
                    } else {
                        playerEntity.sendMessage(Text.literal("Teleporting!").formatted(Formatting.LIGHT_PURPLE), true);
                    }

                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            playerEntity.networkHandler.sendPacket(new ClearTitleS2CPacket(true));
                        }
                    }, 500);

                    timer.cancel();
                    server.submit(counterCallback);
                    return;
                }

                Vec3d currentPos = playerEntity.getPos();
                if (playerEntity.isRemoved()) {
                    playerEntity = server.getPlayerManager().getPlayer(playerEntity.getUuid());
                    assert playerEntity != null;
                } else if (lastPosition.equals(currentPos)) {
                    counter -= .25;
                } else {
                    lastPosition = currentPos;
                    counter = standStillTime;
                }

                if (showCountdownBar) {
                    finalCountDownBar.setPercent((float) (counter / standStillTime));
                } else {
                    playerEntity.sendMessage(Text.literal("Stand still for ").formatted(Formatting.LIGHT_PURPLE)
                        .append(Text.literal(Integer.toString((int) Math.floor(counter + 1))).formatted(Formatting.GOLD))
                        .append(Text.literal(" more seconds!").formatted(Formatting.LIGHT_PURPLE)), true);
                }

                playerEntity.networkHandler.sendPacket(new SubtitleS2CPacket(Text.literal("Please stand still...")
                        .formatted(Formatting.RED, Formatting.ITALIC)));
                playerEntity.networkHandler.sendPacket(new TitleS2CPacket(Text.literal("Teleporting!")
                        .formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD)));
            }
        }, 0, 250);
    }

    public static MutableText FormatText(String name, Text value) {
        if (value.getStyle().getColor() == null)
            return Text.literal(name + ": ").formatted(Formatting.RESET).append(value.copy().formatted(Formatting.GOLD));

        return Text.literal(name + ": ").formatted(Formatting.RESET).append(value);
    }

    public static MutableText FormatText(String name, String value) {
        return FormatText(name, Text.literal(value).formatted(Formatting.GOLD));
    }

    public static MutableText FormatText(String name, double value) {
        return FormatText(name, String.format("%.2f", value));
    }

    public static MutableText FormatText(String name, float value) {
        return FormatText(name, String.format("%.2f", value));
    }

    public static MutableText join(List<Text> values, Text joiner) {
        MutableText out = Text.empty();

        for (int i = 0; i < values.size(); i++) {
            out.append(values.get(i));

            if (i < values.size() - 1)
                out.append(joiner);
        }

        return out;
    }
}

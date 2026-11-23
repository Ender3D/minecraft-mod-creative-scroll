package com.ndrsbox.creativescroll.state;

import com.ndrsbox.creativescroll.Creativescroll;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;

import java.util.ArrayList;
import java.util.List;

public class ScrollSessionManager {

    public static String getFormattedRemainingTime(ServerPlayer player, MinecraftServer server) {
        ScrollSessionData data = player.getAttached(Creativescroll.SCROLL_SESSION);
        if (data == null) return null;

        long remainingTicks = Math.max(0, data.endTick() - server.overworld().getGameTime());
        long mins = remainingTicks / 1200;
        long secs = (remainingTicks % 1200) / 20;
        return String.format("%02d:%02d", mins, secs);
    }

    public static void startSession(ServerPlayer player, int durationTicks) {
        List<ItemStack> savedItems = new ArrayList<>();
        if (Creativescroll.CONFIG.sandboxInventory) {
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                savedItems.add(player.getInventory().getItem(i).copy());
            }
            player.getInventory().clearContent();
        }

        // <-- Line 1 goes here (replaces server.getTickCount())
        long endTick = player.level().getServer().overworld().getGameTime() + durationTicks;
        GameType prevMode = player.gameMode.getGameModeForPlayer();

        ScrollSessionData data = new ScrollSessionData(endTick, prevMode, savedItems);
        player.setAttached(Creativescroll.SCROLL_SESSION, data);
        player.setGameMode(GameType.CREATIVE);
        player.sendSystemMessage(Component.literal("§aScroll of Creative Mode activated!"));
    }

    public static void tickPlayer(ServerPlayer player, MinecraftServer server) {
        ScrollSessionData data = player.getAttached(Creativescroll.SCROLL_SESSION);
        if (data == null) return;

        // <-- Line 2 goes here (replaces server.getTickCount())
        long remaining = data.endTick() - server.overworld().getGameTime();

        if (remaining <= 0) {
            endSession(player, server, data);
            return;
        }

        // Actionbar countdown every second
        if (remaining % 20 == 0) {
            long mins = remaining / 1200;
            long secs = (remaining % 1200) / 20;
            player.sendSystemMessage(
                    Component.literal(String.format("§eCreative Scroll: §f%02d:%02d", mins, secs)), true);
        }
    }

    public static void endSession(ServerPlayer player, MinecraftServer server, ScrollSessionData data) {
        player.removeAttached(Creativescroll.SCROLL_SESSION);

        if (Creativescroll.CONFIG.sandboxInventory) {
            player.containerMenu.setCarried(ItemStack.EMPTY);
            player.inventoryMenu.getCraftSlots().clearContent();
            player.getInventory().clearContent();

            List<ItemStack> saved = data.savedInventory();
            for (int i = 0; i < Math.min(saved.size(), player.getInventory().getContainerSize()); i++) {
                player.getInventory().setItem(i, saved.get(i));
            }
            player.containerMenu.broadcastFullState();
        }

        GameType targetMode = data.previousGameMode() != null ? data.previousGameMode() : server.getDefaultGameType();
        player.setGameMode(targetMode);

        int slowFallTicks = Creativescroll.CONFIG.slowFallingSeconds * 20;
        if (slowFallTicks > 0) {
            player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, slowFallTicks, 0, false, true));
        }

        player.sendSystemMessage(Component.literal("§6Your Scroll of Creative Mode has expired!"));
    }

    public static boolean hasActiveScroll(ServerPlayer player) {
        return player.hasAttached(Creativescroll.SCROLL_SESSION);
    }
}

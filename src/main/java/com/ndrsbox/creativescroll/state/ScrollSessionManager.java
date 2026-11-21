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

    public static void startSession(ServerPlayer player, int durationTicks) {
        List<ItemStack> savedItems = new ArrayList<>();
        if (Creativescroll.CONFIG.sandboxInventory) {
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                savedItems.add(player.getInventory().getItem(i).copy());
            }
            player.getInventory().clearContent();
        }

        ScrollSessionData data = new ScrollSessionData(durationTicks, savedItems);
        player.setAttached(Creativescroll.SCROLL_SESSION, data);
        player.setGameMode(GameType.CREATIVE);
        player.sendSystemMessage(Component.literal("§aScroll of Creative Mode activated!"));
    }

    public static void tickPlayer(ServerPlayer player, MinecraftServer server) {
        ScrollSessionData data = player.getAttached(Creativescroll.SCROLL_SESSION);
        if (data == null) return;

        int remaining = data.remainingTicks() - 1;

        // Actionbar countdown every second
        if (remaining % 20 == 0 && remaining > 0) {
            int mins = remaining / 1200;
            int secs = (remaining % 1200) / 20;
            player.sendSystemMessage(
                    Component.literal(String.format("§eCreative Scroll: §f%02d:%02d", mins, secs)), true);
        }

        if (remaining <= 0) {
            endSession(player, server, data);
        } else {
            player.setAttached(Creativescroll.SCROLL_SESSION,
                    new ScrollSessionData(remaining, data.savedInventory()));
        }
    }

    public static void endSession(ServerPlayer player, MinecraftServer server, ScrollSessionData data) {
        player.removeAttached(Creativescroll.SCROLL_SESSION);

        // Wipe Creative items and restore original Survival inventory
        if (Creativescroll.CONFIG.sandboxInventory) {
            player.getInventory().clearContent();
            List<ItemStack> saved = data.savedInventory();
            for (int i = 0; i < Math.min(saved.size(), player.getInventory().getContainerSize()); i++) {
                player.getInventory().setItem(i, saved.get(i));
            }
        }

        // Revert to server's global default gamemode
        player.setGameMode(server.getDefaultGameType());

        // Apply Slow Falling safeguard
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

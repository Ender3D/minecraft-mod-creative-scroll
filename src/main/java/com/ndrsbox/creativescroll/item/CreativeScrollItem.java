package com.ndrsbox.creativescroll.item;

import com.ndrsbox.creativescroll.Creativescroll;
import com.ndrsbox.creativescroll.state.ScrollSessionManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;

public class CreativeScrollItem extends Item {
    public CreativeScrollItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            if (serverPlayer.gameMode.getGameModeForPlayer() == GameType.CREATIVE) {
                serverPlayer.sendSystemMessage(Component.literal("§cYou are already in Creative Mode!"));
                return InteractionResult.FAIL;
            }

            // Consume 1 scroll
            if (!serverPlayer.getAbilities().instabuild) {
                stack.shrink(1);
            }

            int ticks = Creativescroll.CONFIG.defaultDurationSeconds * 20;
            ScrollSessionManager.startSession(serverPlayer, ticks);

            level.playSound(null, serverPlayer.blockPosition(),
                    SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.0F, 1.0F);
        }

        return InteractionResult.SUCCESS;
    }

    public boolean isFoil(ItemStack stack) {
        return true;
    }
}

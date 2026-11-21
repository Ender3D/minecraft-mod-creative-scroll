package com.ndrsbox.creativescroll.mixin;

import com.ndrsbox.creativescroll.Creativescroll;
import com.ndrsbox.creativescroll.state.ScrollSessionManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin {

    @Shadow
    public ServerPlayer player;

    @Inject(method = "handleSetCreativeModeSlot", at = @At("HEAD"), cancellable = true)
    private void onCreativeSlotClick(ServerboundSetCreativeModeSlotPacket packet, CallbackInfo ci) {
        if (!ScrollSessionManager.hasActiveScroll(this.player)) return;

        // Block throwing items out of the Creative inventory window (slot < 0)
        if (Creativescroll.CONFIG.blockContainersAndDrops && packet.slotNum() < 0 && !packet.itemStack().isEmpty()) {
            this.player.sendSystemMessage(Component.literal("§cYou cannot drop items during Creative Scroll use!"));
            this.player.containerMenu.broadcastFullState();
            ci.cancel();
            return;
        }

        // Block spawning items on the blocklist
        ItemStack clicked = packet.itemStack();
        Identifier itemId = BuiltInRegistries.ITEM.getKey(clicked.getItem());

        if (Creativescroll.CONFIG.blockedItems.contains(itemId.toString())) {
            this.player.sendSystemMessage(Component.literal("§cThat item is blocked during Creative Scroll use!"));
            this.player.containerMenu.broadcastFullState();
            ci.cancel();
        }
    }

    @Inject(method = "handlePlayerAction", at = @At("HEAD"), cancellable = true)
    private void onPlayerAction(ServerboundPlayerActionPacket packet, CallbackInfo ci) {
        if (!Creativescroll.CONFIG.blockContainersAndDrops || !ScrollSessionManager.hasActiveScroll(this.player)) return;

        ServerboundPlayerActionPacket.Action action = packet.getAction();
        if (action == ServerboundPlayerActionPacket.Action.DROP_ITEM
                || action == ServerboundPlayerActionPacket.Action.DROP_ALL_ITEMS) {
            this.player.sendSystemMessage(Component.literal("§cYou cannot drop items during Creative Scroll use!"));
            this.player.containerMenu.broadcastFullState();
            ci.cancel();
        }
    }
}

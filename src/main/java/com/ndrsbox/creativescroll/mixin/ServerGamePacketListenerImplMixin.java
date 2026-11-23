package com.ndrsbox.creativescroll.mixin;

import com.ndrsbox.creativescroll.Creativescroll;
import com.ndrsbox.creativescroll.state.ScrollSessionManager;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.component.ItemContainerContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//import net.minecraft.world.item.ItemStackTemplate;


@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin {

    @Shadow
    public ServerPlayer player;

    @Inject(method = "handleSetCreativeModeSlot", at = @At("HEAD"), cancellable = true)
    private void onCreativeSlotClick(ServerboundSetCreativeModeSlotPacket packet, CallbackInfo ci) {
        if (!ScrollSessionManager.hasActiveScroll(this.player)) return;

        if (Creativescroll.CONFIG.blockContainersAndDrops && packet.slotNum() < 0 && !packet.itemStack().isEmpty()) {
            this.player.sendSystemMessage(Component.literal("§cYou cannot drop items during Creative Scroll use!"));
            this.player.containerMenu.broadcastFullState();
            ci.cancel();
            return;
        }

        if (containsBlockedItem(packet.itemStack())) {
            this.player.sendSystemMessage(Component.literal("§cThat item (or an item inside it) is blocked during Creative Scroll use!"));
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

    @Unique
    private boolean containsBlockedItem(ItemStack stack) {
        if (stack.isEmpty()) return false;

        Identifier itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (Creativescroll.CONFIG.blockedItems.contains(itemId.toString())) {
            return true;
        }

        // Check inside Shulker Boxes / NBT Containers
        ItemContainerContents container = stack.get(DataComponents.CONTAINER);
        if (container != null) {
            for (ItemStack inner : container.nonEmptyItems()) {
                if (containsBlockedItem(inner)) return true;
            }
        }

        // Check inside Bundles
        BundleContents bundle = stack.get(DataComponents.BUNDLE_CONTENTS);
        if (bundle != null) {
            for (ItemStack inner : bundle.items()) {
                if (containsBlockedItem(inner)) return true;
            }
        }

        return false;
    }



}

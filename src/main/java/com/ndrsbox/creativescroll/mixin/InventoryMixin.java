package com.ndrsbox.creativescroll.mixin;

import com.ndrsbox.creativescroll.Creativescroll;
import com.ndrsbox.creativescroll.state.ScrollSessionManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Inventory.class)
public abstract class InventoryMixin {

    @Shadow
    @Final
    public Player player;

    @Inject(method = "dropAll", at = @At("HEAD"), cancellable = true)
    private void onDropAllOnDeath(CallbackInfo ci) {
        if (this.player instanceof ServerPlayer serverPlayer
                && (Creativescroll.CONFIG.blockContainersAndDrops || Creativescroll.CONFIG.sandboxInventory)
                && ScrollSessionManager.hasActiveScroll(serverPlayer)) {
            // Wipe creative items instead of dropping them on death
            ((Inventory) (Object) this).clearContent();
            ci.cancel();
        }
    }
}

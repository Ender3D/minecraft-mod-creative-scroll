package com.ndrsbox.creativescroll.state;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;

import java.util.List;

public record ScrollSessionData(long endTick, GameType previousGameMode, List<ItemStack> savedInventory) {
    public static final Codec<ScrollSessionData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.LONG.fieldOf("end_tick").forGetter(ScrollSessionData::endTick),
                    GameType.CODEC.fieldOf("previous_gamemode").forGetter(ScrollSessionData::previousGameMode),
                    ItemStack.OPTIONAL_CODEC.listOf().fieldOf("saved_inventory").forGetter(ScrollSessionData::savedInventory)
            ).apply(instance, ScrollSessionData::new)
    );
}

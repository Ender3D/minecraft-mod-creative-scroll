package com.ndrsbox.creativescroll.state;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public record ScrollSessionData(int remainingTicks, List<ItemStack> savedInventory) {
    public static final Codec<ScrollSessionData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("remaining_ticks").forGetter(ScrollSessionData::remainingTicks),
                    ItemStack.OPTIONAL_CODEC.listOf().fieldOf("saved_inventory").forGetter(ScrollSessionData::savedInventory)
            ).apply(instance, ScrollSessionData::new)
    );
}

package com.ndrsbox.creativescroll.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class ScrollConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir().resolve("creativescroll.json");

    public int defaultDurationSeconds = 1800; // 30 minutes
    public int slowFallingSeconds = 10;
    public boolean sandboxInventory = true; // Saves survival inv & wipes creative inv on expire
    public boolean blockContainersAndDrops = true; // Stops storing/dropping items while active
    public List<String> blockedItems = List.of(
            "minecraft:bedrock",
            "minecraft:command_block",
            "minecraft:repeating_command_block",
            "minecraft:chain_command_block",
            "minecraft:structure_block",
            "minecraft:barrier",
            "minecraft:light",
            "minecraft:spawner",
            "minecraft:end_portal_frame",
            "minecraft:netherite_block",
            "minecraft:diamond_block"
    );

    public static ScrollConfig load() {
        if (Files.exists(CONFIG_PATH)) {
            try {
                return GSON.fromJson(Files.readString(CONFIG_PATH), ScrollConfig.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        ScrollConfig defaults = new ScrollConfig();
        defaults.save();
        return defaults;
    }

    public void save() {
        try {
            Files.writeString(CONFIG_PATH, GSON.toJson(this));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

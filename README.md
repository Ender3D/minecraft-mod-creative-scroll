# Scroll of Creative Mode (Timed & Sandboxed Admin Rewards)

Giving non-admin players temporary Creative Mode for large-scale building projects or event rewards is normally a massive security risk. 
Standard **/gamemode commands** lack built-in timers, leave players vulnerable to fatal fall damage when flight suddenly cuts out, and allow unrestricted spawning and hoarding of bedrock, command blocks, or endless stacks of rare resources.

**Scroll of Creative Mode** introduces a completely independent, admin-issued consumable item that isolates temporary Creative access into timed, sandboxed sessions. 
It empowers server moderators to grant risk-free Creative building privileges without compromising server economy or world integrity.

## Features

- **Admin-Exclusive Custom Item:** Independent item registration name (creativescroll:creative_scroll). Excluded from all survival crafting recipes and Creative tabs so it can only be spawned and distributed by authorized moderators.

- **Persistent Session Countdown:** Tracks remaining session time via server-side entity data attachments, complete with a live action bar timer (MM:SS). Timers persist seamlessly across player disconnects and server restarts before automatically reverting the player to the server's global default gamemode.

- **Inventory Sandboxing:** Captures and stores a player's original Survival inventory upon activation, wipes all Creative-spawned items when the timer expires, and restores their exact pre-session inventory intact.

- **Packet-Level Item Blocklist:** Intercepts ServerboundSetCreativeModeSlotPacket transmissions directly on the server to reject restricted blocks and items before they can ever be spawned—protecting your server even against modified clients.

- **Container & Drop Isolation:** Actively blocks players from opening chests, barrels, shulker boxes, furnaces, hoppers, item frames, or dropping items (Q or drag-out) while a scroll session is active, preventing item smuggling into permanent storage.

- **Safe-Landing Expiration Safeguard:** Automatically applies a configurable Slow Falling status effect the moment the scroll expires, protecting players who are mid-flight high above their builds when Creative flight disables.

- **Live Admin Management:** Includes built-in operator commands to reload configuration rules on the fly or terminate an active session early without requiring a server restart.

## How It Works

Traditional gamemode-switching setups rely on external permission plugins or fragile command block loops that leave wide gaps for item duplication and storage exploits. **Scroll of Creative Mode** enforces strict state isolation directly at the server tick and network packet layers.

When a player right-clicks with the scroll, the server consumes the item, serializes their current inventory into a persistent attachment record, switches their gamemode to Creative, and initiates a tick-accurate countdown.

While active, server-side Mixins and Fabric interaction callbacks inspect incoming slot packets, player drop actions, and container interactions—blocking forbidden item IDs and storage access in real time.  

Once the countdown reaches zero (or an admin forces expiration), the player's Creative inventory is purged, their original Survival gear is restored, their gamemode reverts to the server default, and Slow Falling engages for a safe descent.

# Instructions for Use

## Installation

Ensure your server and client environments are running the Fabric Loader for Minecraft 26.3 alongside Fabric API.
Drop the creativescroll-*.jar file into both your server and client mods/ directories.
Launch the game once to auto-generate your custom server parameters profile.

## Obtaining & Using the Scroll

Because the Scroll of Creative Mode is restricted to administrative distribution, it has no survival crafting recipe.

**Spawn the Item (Admins / Operators):**\
/give <player> creativescroll:creative_scroll

### Activate:
Hold the scroll and Right-Click at any time to consume the item and begin your timed Creative Mode session.
Admin Commands:

**Operators (Gamemaster / OP Level 2+) can manage configurations and active sessions live in-game:**\
**/CreativeScroll reload** — Reloads config/creativescroll.json immediately without restarting the server.\
**/CreativeScroll expire** <player> — Immediately terminates a target player's active scroll session, restores their Survival inventory, reverts their gamemode, and applies Slow Falling.

## Configuration Profile Tuning:

Navigate to your instance directory path at /config/creativescroll.json to fine-tune your session durations, anti-cheat protections, and blocked item list:

```jason
{
  "defaultDurationSeconds": 1800,
  "slowFallingSeconds": 10,
  "sandboxInventory": true,
  "blockContainersAndDrops": true,
  "blockedItems": [
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
  ]
}
```

- **defaultDurationSeconds:** Total duration of a single scroll session in seconds (default 1800 = 30 minutes).
- **slowFallingSeconds:** Duration in seconds of the Slow Falling safety effect applied when a session expires (set to 0 to disable).
- **sandboxInventory:** Set to true to back up the player's Survival inventory on activation and wipe Creative-spawned items upon expiration.
- **blockContainersAndDrops:** Set to true to prevent players from opening storage containers, interacting with item frames/armor stands, or dropping items while a scroll is active.
- **blockedItems:** A customizable list of namespaced item IDs (e.g., "minecraft:gold_block", "minecraft:chest") that players are prohibited from spawning from the Creative menu.

## License

This mod is licensed under the [MIT License](LICENSE).
package com.ndrsbox.creativescroll;

import com.ndrsbox.creativescroll.config.ScrollConfig;
import com.ndrsbox.creativescroll.item.CreativeScrollItem;
import com.ndrsbox.creativescroll.state.ScrollSessionData;
import com.ndrsbox.creativescroll.state.ScrollSessionManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Creativescroll implements ModInitializer {
	public static final String MOD_ID = "creativescroll";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static ScrollConfig CONFIG;

	public static final AttachmentType<ScrollSessionData> SCROLL_SESSION = AttachmentRegistry.create(
			Identifier.fromNamespaceAndPath(MOD_ID, "scroll_session"),
			builder -> builder
					.persistent(ScrollSessionData.CODEC)
					.copyOnDeath()
	);

	public static final ResourceKey<Item> SCROLL_KEY = ResourceKey.create(
			Registries.ITEM,
			Identifier.fromNamespaceAndPath(MOD_ID, "creative_scroll")
	);

	public static final Item CREATIVE_SCROLL = Registry.register(
			BuiltInRegistries.ITEM,
			SCROLL_KEY,
			new CreativeScrollItem(new Item.Properties().setId(SCROLL_KEY))
	);

	@Override
	public void onInitialize() {
		CONFIG = ScrollConfig.load();

		// Tick active scroll timers every server tick
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			for (ServerPlayer player : server.getPlayerList().getPlayers()) {
				ScrollSessionManager.tickPlayer(player, server);
			}
		});

		// Prevent opening chests, barrels, shulkers, etc. while scroll is active
		UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
			if (!world.isClientSide() && player instanceof ServerPlayer serverPlayer
					&& CONFIG.blockContainersAndDrops
					&& ScrollSessionManager.hasActiveScroll(serverPlayer)) {
				BlockEntity be = world.getBlockEntity(hitResult.getBlockPos());
				if (be instanceof MenuProvider || be instanceof Container) {
					serverPlayer.sendSystemMessage(Component.literal("§cYou cannot open storage containers during Creative Scroll use!"));
					return InteractionResult.FAIL;
				}
			}
			return InteractionResult.PASS;
		});

		// Prevent stashing items in minecart chests, item frames, or armor stands
		UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
			if (!world.isClientSide() && player instanceof ServerPlayer serverPlayer
					&& CONFIG.blockContainersAndDrops
					&& ScrollSessionManager.hasActiveScroll(serverPlayer)) {
				if (entity instanceof Container || entity instanceof ItemFrame || entity instanceof ArmorStand) {
					serverPlayer.sendSystemMessage(Component.literal("§cYou cannot store items in entities during Creative Scroll use!"));
					return InteractionResult.FAIL;
				}
			}
			return InteractionResult.PASS;
		});

		// Register /scroll admin commands (requires Gamemaster / OP level 2)
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(Commands.literal("CreativeScroll")
					.requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
					.then(Commands.literal("reload")
							.executes(context -> {
								CONFIG = ScrollConfig.load();
								context.getSource().sendSuccess(
										() -> Component.literal("§aCreative Scroll config reloaded!"), true);
								return 1;
							})
					)
					.then(Commands.literal("expire")
							.then(Commands.argument("target", EntityArgument.player())
									.executes(context -> {
										ServerPlayer target = EntityArgument.getPlayer(context, "target");
										ScrollSessionData data = target.getAttached(SCROLL_SESSION);
										if (data == null) {
											context.getSource().sendFailure(
													Component.literal("§cThat player does not have an active scroll."));
											return 0;
										}
										ScrollSessionManager.endSession(target, context.getSource().getServer(), data);
										context.getSource().sendSuccess(
												() -> Component.literal("§eExpired Creative Scroll for " + target.getName().getString()), true);
										return 1;
									})
							)
					)
			);
		});

		LOGGER.info("Scroll of Creative Mode initialized!");
	}
}

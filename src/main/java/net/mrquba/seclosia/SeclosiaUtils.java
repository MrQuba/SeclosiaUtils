package net.mrquba.seclosia;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;
import net.minecraft.util.math.Vec3i;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;

public class SeclosiaUtils implements ModInitializer {
	public List<Pair<String, String>> commands = new LinkedList<>();
	public List<String> CommandsToRemove = new LinkedList<>();
	public List<String> LocationsToRemove = new LinkedList<>();
	public List<Pair<String, Vec3i>> locations = new LinkedList<>();
	public static final String MOD_ID = "seclosiautils";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	SeclosiaUtilsConfig conf = new SeclosiaUtilsConfig();

	private static CommandDispatcher<ServerCommandSource> dispatcher;

	@Override
	public void onInitialize() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			SeclosiaUtils.dispatcher = dispatcher;
			this.commands = conf.loadConfig(SeclosiaUtilsConfig.COMMANDS_FILE);
			init(SeclosiaUtils.dispatcher);
			this.commands.clear();
			this.locations = conf.loadConfig(SeclosiaUtilsConfig.LOCATIONS_FILE, true);
			init(SeclosiaUtils.dispatcher, true);
			executionCommandRegistration(dispatcher, "get_seed", "seed");
			addCommandRegistration(dispatcher);
			removeCommandRegistration(dispatcher);
		});
		ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
			LOGGER.info("Saving commands");
			saveConfig(SeclosiaUtilsConfig.COMMANDS_FILE, this.commands, this.CommandsToRemove);
			LOGGER.info("Saving locations");
			saveConfig(SeclosiaUtilsConfig.LOCATIONS_FILE, this.locations, this.LocationsToRemove);
		});
	}

	private void removeCommandRegistration(CommandDispatcher<ServerCommandSource> dispatcher) {
		dispatcher.register(CommandManager.literal("seclosia")
				.then(CommandManager.literal("remove")
						.then(removeSubCommand("shortcut", this.CommandsToRemove))
						.then(removeSubCommand("location", this.LocationsToRemove))
						));
	}
	private LiteralArgumentBuilder<ServerCommandSource> removeSubCommand(String literal, List<String> list){
		return CommandManager.literal(literal)
				.then(CommandManager.argument("name", StringArgumentType.word())
					.executes(context -> {
						list.add((StringArgumentType.getString(context, "name")));
						System.out.println(list);
						return  1;
				}));
	}
	private void executionCommandRegistration(CommandDispatcher<ServerCommandSource> dispatcher, String args, String result) {
		dispatcher.register(CommandManager.literal("seclosia")
				.then(CommandManager.literal(args)
						.executes(context -> {
							ServerCommandSource commandSource = context.getSource();
							dispatcher.execute(result, commandSource);
							context.getSource().sendFeedback(() ->
									Text.literal("Executed command %s".formatted(result)), false);
							return 1;
						}))
		);
	}
	private void executionCommandRegistration(CommandDispatcher<ServerCommandSource> dispatcher, String name, Vec3i pos) {
		dispatcher.register(CommandManager.literal("seclosia")
				.then(CommandManager.literal("location")
						.then(CommandManager.literal(name)
						.executes(context -> {
							context.getSource().sendFeedback(() ->
									Text.literal("Location of %s is: x %s, y %s, z %s".formatted(name, pos.getX(), pos.getY(), pos.getZ())), false);
							return 1;
						})))
		);
	}

	private void addCommandRegistration(CommandDispatcher<ServerCommandSource> dispatcher) {
		dispatcher.register(CommandManager.literal("seclosia")
				.then(CommandManager.literal("add")
						.then(CommandManager.argument("shortcut", StringArgumentType.string())
								.then(CommandManager.argument("command", StringArgumentType.greedyString())
										.executes(context -> {
											String shortcut = StringArgumentType.getString(context, "shortcut");
											String command = StringArgumentType.getString(context, "command");
											this.commands.add(new Pair<>(shortcut, command));
											context.getSource().sendFeedback(() ->
													Text.literal("Added shortcut %s for command: %s".formatted(shortcut, command)), false);
											init(SeclosiaUtils.dispatcher);
											return 1;
										})))
						.then(CommandManager.literal("location")
								.then(CommandManager.argument("name", StringArgumentType.string())
										.then(CommandManager.argument("x", IntegerArgumentType.integer())
														.then(CommandManager.argument("y", IntegerArgumentType.integer())
																.then(CommandManager.argument("z", IntegerArgumentType.integer())
																		.executes(context -> {
																			String locationName = StringArgumentType.getString(context, "name");
																			Vec3i locationPos = new Vec3i(IntegerArgumentType.getInteger(context, "x"),
																					IntegerArgumentType.getInteger(context, "y"),
																					IntegerArgumentType.getInteger(context, "z"));
																			this.locations.add(new Pair<>(locationName, locationPos));
																			context.getSource().sendFeedback(() ->
																					Text.literal("Added location %s at coordinates: %s".formatted(locationName, locationPos)), false);
																			init(SeclosiaUtils.dispatcher, true);
                                                                            return 1;
                                                                        })
																)
														)
										)
								)
						))
		);
	}

	private void init(CommandDispatcher<ServerCommandSource> dispatcher) {
		for (Pair<String, String> s : this.commands) {
			executionCommandRegistration(dispatcher, s.getLeft(), s.getRight());
		}
		conf.saveConfig(SeclosiaUtilsConfig.COMMANDS_FILE, this.commands, this.CommandsToRemove);
		this.commands.clear();
		this.CommandsToRemove.clear();
	}
	private <T, U> void saveConfig(File file, List<Pair<T, U>> lp, List<String> ctr){

		conf.saveConfig(file, lp, ctr);
		lp.clear();
		ctr.clear();
	}
	private void init(CommandDispatcher<ServerCommandSource> dispatcher, boolean location) {
		for (Pair<String, Vec3i> s : this.locations) {
			executionCommandRegistration(dispatcher, s.getLeft(), s.getRight());
		}
		conf.saveConfig(SeclosiaUtilsConfig.LOCATIONS_FILE, this.locations, this.LocationsToRemove);
		this.locations.clear();
		this.LocationsToRemove.clear();
	}
}
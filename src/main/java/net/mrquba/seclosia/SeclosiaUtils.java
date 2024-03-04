package net.mrquba.seclosia;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;
import net.minecraft.util.math.Vec3i;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import java.util.HashSet;
import java.util.Set;

public class SeclosiaUtils implements ModInitializer {
	public List<Pair<String, String>> commands = new LinkedList<>();
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
			executionCommandRegistration(dispatcher, "seclosia", "get_seed", "seed");
			addCommandRegistration(dispatcher, "seclosia");
		});
	}

	private void executionCommandRegistration(CommandDispatcher<ServerCommandSource> dispatcher, String literal, String args, String result) {
		dispatcher.register(CommandManager.literal(literal)
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
	private void executionCommandRegistration(CommandDispatcher<ServerCommandSource> dispatcher, String literal, String args, String name, Vec3i pos) {
		dispatcher.register(CommandManager.literal(literal)
				.then(CommandManager.literal(args)
						.then(CommandManager.literal(name)
						.executes(context -> {
							context.getSource().sendFeedback(() ->
									Text.literal("Location of %s is: x %s, y %s, z %s".formatted(name, pos.getX(), pos.getY(), pos.getZ())), false);
							return 1;
						})))
		);
	}

	private void addCommandRegistration(CommandDispatcher<ServerCommandSource> dispatcher, String literal) {
		dispatcher.register(CommandManager.literal(literal)
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
			executionCommandRegistration(dispatcher, "seclosia", s.getLeft(), s.getRight());
		}
		conf.saveConfig(SeclosiaUtilsConfig.COMMANDS_FILE, this.commands);
		this.commands.clear();
	}
	private void init(CommandDispatcher<ServerCommandSource> dispatcher, boolean location) {
		for (Pair<String, Vec3i> s : this.locations) {
			executionCommandRegistration(dispatcher, "seclosia", "location", s.getLeft(), s.getRight());
		}
		conf.saveConfig(SeclosiaUtilsConfig.LOCATIONS_FILE, this.locations);
		this.locations.clear();
	}
}
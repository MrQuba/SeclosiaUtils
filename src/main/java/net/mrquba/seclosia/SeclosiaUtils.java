package net.mrquba.seclosia;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;
import net.minecraft.util.math.Vec3i;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class SeclosiaUtils implements ModInitializer {
	public List<Pair<String, String>> commands = new LinkedList<>();
	public List<String> CommandsToRemove = new LinkedList<>();
	public List<String> LocationsToRemove = new LinkedList<>();
	public List<String> MacrosToRemove = new LinkedList<>();
	public List<Pair<String, Vec3i>> locations = new LinkedList<>();
	public static final String MOD_ID = "seclosiautils";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	SeclosiaUtilsConfig conf = new SeclosiaUtilsConfig();
	public  List<Pair<String, String>> macros = new LinkedList<>();
	private double mspt;
	private double tps;
	private long lastTickTime  = 0;
	private long tickCount = 0;
	private boolean tpsEnabled = false;
	private boolean msptEnabled = false;

	private static CommandDispatcher<ServerCommandSource> dispatcher;
@FunctionalInterface
interface getVar{
	double get();
}
	@FunctionalInterface
	interface toggle{
		double toggle(CommandContext<ServerCommandSource> context);
	}
	@Override
	public void onInitialize() {
		ServerTickEvents.END_SERVER_TICK.register(this::onEndServerTick);
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			SeclosiaUtils.dispatcher = dispatcher;
			CommandUtils.onRegisterEvent(dispatcher,this.commands, SeclosiaUtilsConfig.COMMANDS_FILE, this::executionCommandRegistration, this.CommandsToRemove);
			CommandUtils.onRegisterEvent(dispatcher,this.locations, SeclosiaUtilsConfig.LOCATIONS_FILE, this::locationRegistration, this.LocationsToRemove, true);
			CommandUtils.onRegisterEvent(dispatcher,this.macros, SeclosiaUtilsConfig.MACROS_FILE, this::macroRegistration, this.MacrosToRemove);
			executionCommandRegistration(dispatcher, "get_seed", "seed");
			addCommandRegistration(dispatcher);
			removeCommandRegistration(dispatcher);
			updateGetCommands(dispatcher);
			switchCommandRegistration(dispatcher);
		});
		ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
			LOGGER.info("Saving commands");
			CommandUtils.saveConfig(SeclosiaUtilsConfig.COMMANDS_FILE, this.commands, this.CommandsToRemove);
			LOGGER.info("Saving locations");
			CommandUtils.saveConfig(SeclosiaUtilsConfig.LOCATIONS_FILE, this.locations, this.LocationsToRemove);
			LOGGER.info("Saving macros");
			CommandUtils.saveConfig(SeclosiaUtilsConfig.MACROS_FILE, this.macros, this.MacrosToRemove);
		});
	}
	private void onEndServerTick(MinecraftServer server){
		// MSPT
		if(msptEnabled){
			this.mspt = Arrays.stream(server.getTickTimes()).average().orElse(Double.NaN) /1000000;
			this.mspt = (double) Math.round(mspt * 10) / 10;
		}
		else mspt = -1;
		//TPS
		if(tpsEnabled){
			long currentTime = System.nanoTime();
			tickCount++;
			if(tickCount % 20 == 0){
				long timeElapsed = currentTime - lastTickTime;
				this.tps = 1_000_000_000.0 / (timeElapsed / 20.0);
				this.tps = (double)Math.round(this.tps * 10) / 10;
				lastTickTime = currentTime;
			}
		}
		else tps = -1;
	}
	private void removeCommandRegistration(CommandDispatcher<ServerCommandSource> dispatcher) {
		dispatcher.register(CommandManager.literal("seclosia")
				.then(CommandManager.literal("remove")
						.then(removeSubCommand("shortcut", this.CommandsToRemove))
						.then(removeSubCommand("location", this.LocationsToRemove))
						.then(removeSubCommand("macro", this.MacrosToRemove))
						));
	}
	private void switchCommandRegistration(CommandDispatcher<ServerCommandSource> dispatcher) {
		dispatcher.register(CommandManager.literal("seclosia")
				.then(CommandManager.literal("enable")
						.then(CommandManager.literal("tps")
								.executes(this::toggleTPS))
						.then(CommandManager.literal("mspt")
								.executes(this::toggleMSPT))
				));
	}
	private int toggleMSPT(CommandContext<ServerCommandSource> context) {
		msptEnabled = !msptEnabled;

		context.getSource().sendFeedback(() ->
				Text.literal("Switch: %s".formatted(msptEnabled)), false);

		return 1;
	}
	private int toggleTPS(CommandContext<ServerCommandSource> context) {
		tpsEnabled = !tpsEnabled;

		context.getSource().sendFeedback(() ->
				Text.literal("Switch: %s".formatted(tpsEnabled)), false);

		return 1;
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
	double getMspt () { return this.mspt; }
	double getTps () { return this.tps; }
	private void displayVariableCommandRegistration(CommandDispatcher<ServerCommandSource> dispatcher, String literal, String varDisplayText, getVar var) {
		dispatcher.register(CommandManager.literal("seclosia")
				.then(CommandManager.literal("get")
						.then(CommandManager.literal(literal)
						.executes(context -> displayVariableCommandDisplay(context, varDisplayText, var))))
		);
	}
	private int displayVariableCommandDisplay(CommandContext<ServerCommandSource> context, String varDisplayText, getVar var){
		System.out.println(var.get());
		context.getSource().sendFeedback(() -> Text.literal("%s %s".formatted(varDisplayText, (var.get() < 0) ? "is not being tracked" : var.get())), true);
		return 1;
	}
	private void locationRegistration(CommandDispatcher<ServerCommandSource> dispatcher, String name, Vec3i pos) {
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
	private void macroRegistration(CommandDispatcher<ServerCommandSource> dispatcher, String name, String string) {
		dispatcher.register(CommandManager.literal("seclosia")
				.then(CommandManager.literal("macro")
						.then(CommandManager.literal(name)
								.executes(context -> {
									context.getSource().sendFeedback(() ->
											Text.literal("Executing macro %s".formatted(name)), false);
									String[] splitResults = string.split(",");
									for(String s : splitResults){
										context.getSource().sendFeedback(() ->
												Text.literal("Executing %s".formatted(s)), false);
										dispatcher.execute(s, context.getSource());
									}
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
											CommandUtils.init(dispatcher, this.commands, SeclosiaUtilsConfig.COMMANDS_FILE, this::executionCommandRegistration, this.CommandsToRemove);
											return 1;
										})))

						.then(CommandManager.literal("macro")
								.then(CommandManager.argument("name", StringArgumentType.string())
								.then(CommandManager.argument("commands", StringArgumentType.greedyString())
										.executes(context -> {
											String macro = StringArgumentType.getString(context, "name");
											String string = StringArgumentType.getString(context, "commands");
											this.macros.add(new Pair<>(macro,string));
											context.getSource().sendFeedback(() ->
													Text.literal("Added macro %s".formatted(macro)), false);
											CommandUtils.init(dispatcher, this.macros, SeclosiaUtilsConfig.MACROS_FILE, this::macroRegistration, this.MacrosToRemove);
											return 1;
										}))))
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
																			CommandUtils.init(dispatcher, this.locations, SeclosiaUtilsConfig.LOCATIONS_FILE, this::locationRegistration, this.LocationsToRemove);
                                                                            return 1;
                                                                        })
																)
														)
										)
								)
						))
		);
	}
	private void updateGetCommands(CommandDispatcher<ServerCommandSource> dispatcher){
		displayVariableCommandRegistration(dispatcher, "mspt", "MSPT: ", this::getMspt);
		displayVariableCommandRegistration(dispatcher, "tps", "TPS: ", this::getTps);
	}
}
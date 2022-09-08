package com.github.sanctum.jda.ui.content;

import com.github.sanctum.jda.GuppyAPI;
import com.github.sanctum.jda.GuppyEntryPoint;
import com.github.sanctum.jda.addon.DiscordExtensionManager;
import com.github.sanctum.jda.common.Command;
import com.github.sanctum.jda.common.Guppy;
import com.github.sanctum.jda.listener.JDAListenerAdapter;
import com.github.sanctum.jda.loading.DockingAgent;
import com.github.sanctum.jda.ui.api.ConsoleCommand;
import com.github.sanctum.jda.util.OptionTypeConverter;
import com.github.sanctum.panther.container.PantherArrays;
import com.github.sanctum.panther.container.PantherCollection;
import com.github.sanctum.panther.container.PantherList;
import com.github.sanctum.panther.util.PantherLogger;
import com.github.sanctum.panther.util.SimpleAsynchronousTask;
import java.io.File;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.jetbrains.annotations.NotNull;

public class StartConsoleCommand extends ConsoleCommand {
	public StartConsoleCommand() {
		super("start");
		setAliases(PantherArrays.asList("st", "start.sh"));
	}

	@Override
	public void execute(String[] arguments) {
		PantherLogger.getInstance().setLogger(Logger.getLogger(Guppy.class.getSimpleName()));
		Activity activity = Activity.watching("Aqua Teen Hunger Force.");
		Scanner input = new Scanner(System.in);
		String nextInput = input.nextLine();
		System.out.println(nextInput);
		Logger logger = PantherLogger.getInstance().getLogger();
		logger.info("---------------------------");
		logger.info("Loading properties...");
		logger.info("---------------------------");
		GuppyEntryPoint.setInstance(new GuppyEntryPoint(PantherLogger.getInstance().getLogger()));
		try {
			GuppyEntryPoint.getInstance().enable(new DockingAgent().consume(new DockingAgent.Procedure() {
				@Override
				public void onConstruct(@NotNull JDABuilder builder) {
					builder.setToken("NzgwNTc4MTU4NTk2NDU2NDc4.GyX1mM.oZ8pFvF6aUHO1kOhwa_WcUbt7EAl6nnk150MVQ");
					builder.enableIntents(Arrays.asList(GatewayIntent.values()));
					builder.setActivity(activity);
					builder.addEventListeners(new JDAListenerAdapter());
					builder.enableCache(CacheFlag.ACTIVITY, CacheFlag.EMOJI, CacheFlag.VOICE_STATE, CacheFlag.ONLINE_STATUS);

					builder.setChunkingFilter(ChunkingFilter.ALL);
					builder.setMemberCachePolicy(MemberCachePolicy.ALL);
					builder.setLargeThreshold(300);
				}

				@Override
				public void onFinalize(@NotNull JDA instance) {
					PantherCollection<SlashCommandData> data = new PantherList<>();
					for (Command c : GuppyAPI.getInstance().getCommands().getAll()) {
						SlashCommandData slashCommand = Commands.slash(c.getLabel(), c.getDescription());
						for (Command.Option o : c.getOptions().get()) {
							OptionType type = OptionTypeConverter.get(o);
							slashCommand.addOption(type, o.getName(), o.getDescription());
						}
						slashCommand.setGuildOnly(true);
						data.add(slashCommand);
					}
					// clear cache.
					instance.updateCommands().queue();
					instance.updateCommands().addCommands(data.stream().toArray(CommandData[]::new)).queue();
				}
			}));
			logger.info("Bot Loaded.");
			logger.info("---------------------------");
		} catch (Exception e) {
			logger.info("Bot loading failure, reason: " + e.getMessage());
			logger.info("---------------------------");
		}
		logger.info("Say " + '"' + "stop" + '"' + " or " + '"' + "exit" + '"' + " to close this application.");
		logger.info("---------------------------");
		File addonFolder = DiscordExtensionManager.getInstance().getAddonFolder();
		if (!addonFolder.exists()) {
			addonFolder.mkdirs();
		}
	}
}

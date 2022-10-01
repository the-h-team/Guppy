package com.github.sanctum.jda.common.content;

import com.github.sanctum.jda.GuppyEntryPoint;
import com.github.sanctum.jda.addon.DiscordExtensionManager;
import com.github.sanctum.jda.common.api.ConsoleCommand;
import com.github.sanctum.panther.container.PantherArrays;
import com.github.sanctum.panther.util.PantherLogger;
import net.dv8tion.jda.api.JDA;

public class CommandStop extends ConsoleCommand {
	public CommandStop() {
		super("stop");
		setAliases(PantherArrays.asList("quit", "exit"));
	}

	@Override
	public void execute(String[] arguments) {
		if (arguments.length == 0) {
			DiscordExtensionManager.getInstance().getExtensions().forEach(e -> {
				if (e.getValue().isActive()) {
					PantherLogger.getInstance().getLogger().info("Extension " + '"' + e.getValue().getClass().getSimpleName() + '"' + " disabling.");
					DiscordExtensionManager.getInstance().remove(e.getValue());
				}
			});
			GuppyEntryPoint entryPoint = GuppyEntryPoint.getInstance();
			if (entryPoint != null) {
				JDA jda = entryPoint.getJDA();
				if (jda != null) jda.shutdown();
			}
			System.exit(0);
		}
	}
}

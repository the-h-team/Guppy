package com.github.sanctum.jda.common.content;

import com.github.sanctum.jda.addon.DiscordClassLoader;
import com.github.sanctum.jda.addon.DiscordExtensionManager;
import com.github.sanctum.jda.common.api.ConsoleCommand;
import com.github.sanctum.panther.util.PantherLogger;
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

public class CommandAddon extends ConsoleCommand {
	public CommandAddon() {
		super("addon");
	}

	@Override
	public void execute(String[] args) {
		if (args.length == 0) {
			Logger logger = PantherLogger.getInstance().getLogger();

		}
		if (args.length == 1) {
			if (args[0].equalsIgnoreCase("find")) {
				Logger logger = PantherLogger.getInstance().getLogger();
				logger.info("Looking for not yet loaded extensions...");
				File directory = DiscordExtensionManager.getInstance().getAddonFolder();
				File[] ar = directory.listFiles();
				if (ar != null && ar.length > 0) {
					for (File f : ar) {
						if (f.isFile() && f.getName().endsWith(".jar")) {
							try {
								DiscordClassLoader loader = new DiscordClassLoader(f) {
								};
								if (loader.isLoaded()) {
									logger.info("Extension " + '"' + loader.getMainClass().getClass().getSimpleName() + '"' + " already loaded, skipping...");
								} else {
									logger.info("Loaded extension " + loader.getMainClass().getClass().getSimpleName());
									DiscordExtensionManager.getInstance().load(loader.getMainClass());
								}
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}
				} else {
					logger.info("There are no extensions to load.");
				}
			}
		}
	}
}

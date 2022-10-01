package com.github.sanctum.jda.addon;

import com.github.sanctum.panther.container.PantherEntryMap;
import com.github.sanctum.panther.container.PantherMap;
import java.io.File;
import org.jetbrains.annotations.NotNull;

public final class DiscordExtensionManager {

	static DiscordExtensionManager instance;
	final PantherMap<String, DiscordExtension> extensions = new PantherEntryMap<>();
	final File addonFolder = new File("addons/Test.json").getParentFile();

	public DiscordExtensionManager load(@NotNull DiscordExtension extension) {
		if (!extension.isActive()) {
			extension.onLoad();
			extensions.put(extension.getClass().getSimpleName(), extension);
			extension.setActive(true);
			extension.onEnable();
		}
		return this;
	}

	public DiscordExtensionManager remove(@NotNull DiscordExtension extension) {
		if (extension.isActive()) {
			extension.onDisable();
			extension.setActive(false);
			extensions.remove(extension.getClass().getSimpleName());
		}
		return this;
	}

	public boolean contains(@NotNull Class<? extends DiscordExtension> c) {
		return extensions.containsKey(c.getSimpleName());
	}

	public boolean isActive(@NotNull Class<? extends DiscordExtension> c) {
		return extensions.containsKey(c.getSimpleName()) && extensions.get(c.getSimpleName()).isActive();
	}

	public PantherMap<String, DiscordExtension> getExtensions() {
		return extensions;
	}

	public File getAddonFolder() {
		return addonFolder;
	}

	public static @NotNull DiscordExtensionManager getInstance() {
		return instance != null ? instance : (instance = new DiscordExtensionManager());
	}

}

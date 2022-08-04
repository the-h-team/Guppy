package com.github.sanctum.jda;

import com.github.sanctum.jda.common.GuppyConfigurable;
import com.github.sanctum.jda.util.InvalidGuppyStateException;
import com.github.sanctum.labyrinth.data.FileList;
import com.github.sanctum.labyrinth.data.FileManager;
import com.github.sanctum.panther.recursive.ServiceFactory;
import org.bukkit.plugin.java.JavaPlugin;

public final class GuppySpigotPlugin extends JavaPlugin {

	final GuppyEntryPoint entryPoint = new GuppyEntryPoint(getLogger());

	@Override
	public void onDisable() {
		try {
			entryPoint.disable();
		} catch (InvalidGuppyStateException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onEnable() {
		try {
			GuppyConfigurable g = ServiceFactory.getInstance().getService(GuppyConfigurable.class);
			if (g == null) {
				FileList fileList = FileList.search(this);
				FileManager manager = fileList.get("config");
				if (!manager.getRoot().exists()) {
					fileList.copyYML("config", manager);
				}
				GuppyConfigurable configurable = new GuppyConfigurable();
				configurable.set(manager.getRoot());
				ServiceFactory.getInstance().getLoader(GuppyConfigurable.class).supply(configurable);
			}
			entryPoint.enable(null);
		} catch (InterruptedException | InvalidGuppyStateException e) {
			getLogger().severe("An issue occurred while trying to activate guppy: " + e.getMessage());
			getServer().getPluginManager().disablePlugin(this);
		}
	}
}

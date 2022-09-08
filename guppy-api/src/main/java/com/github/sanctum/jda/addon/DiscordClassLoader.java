package com.github.sanctum.jda.addon;

import com.github.sanctum.jda.common.Guppy;
import com.github.sanctum.panther.util.AbstractClassLoader;
import java.io.File;
import java.io.IOException;

public class DiscordClassLoader extends AbstractClassLoader<DiscordExtension> {
	protected DiscordClassLoader(File file) throws IOException {
		super(file, Guppy.class.getClassLoader());
	}

	public boolean isLoaded() {
		return DiscordExtensionManager.getInstance().isActive(getMainClass().getClass());
	}

}

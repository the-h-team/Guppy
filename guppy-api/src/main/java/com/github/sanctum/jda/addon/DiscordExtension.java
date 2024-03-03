package com.github.sanctum.jda.addon;

import java.util.concurrent.TimeUnit;
import org.jetbrains.annotations.NotNull;

public abstract class DiscordExtension {

	boolean active;

	public void onLoad() {}

	public void onEnable() {}

	public void onDisable() {}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public interface Header {

		@NotNull String[] get();

	}

	public interface Footer {

		@NotNull String[] get();

		@NotNull FooterFormat getFormat();

	}

	@FunctionalInterface()
	public interface FooterFormat {

		FooterFormat MINECRAFT = () -> "Done (%ss)! For help, type " + '"' + "help" + '"';
		FooterFormat STANDARD = () -> "Finished loading. Type " + '"' + "help" + '"';
		FooterFormat UNKNOWN = () -> "Unknown loading footer format.";

		@NotNull String get();

	}

}

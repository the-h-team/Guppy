package com.github.sanctum.jda.addon;

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
}

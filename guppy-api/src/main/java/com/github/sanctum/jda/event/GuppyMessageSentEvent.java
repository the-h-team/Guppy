package com.github.sanctum.jda.event;

import com.github.sanctum.jda.common.Guppy;
import org.jetbrains.annotations.NotNull;

public class GuppyMessageSentEvent extends GuppyMessageEvent {
	final boolean isToBot;

	public GuppyMessageSentEvent(@NotNull Guppy guppy, Guppy.@NotNull Message message, boolean toBot) {
		super(guppy, message);
		this.isToBot = toBot;
	}

	public boolean isToBot() {
		return isToBot;
	}
}

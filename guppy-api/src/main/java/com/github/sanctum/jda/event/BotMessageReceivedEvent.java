package com.github.sanctum.jda.event;

import com.github.sanctum.jda.common.Guppy;
import org.jetbrains.annotations.NotNull;

public class BotMessageReceivedEvent extends GuppyMessageSentEvent {

	public BotMessageReceivedEvent(@NotNull Guppy guppy, @NotNull Guppy.Message message) {
		super(guppy, message, true);
	}

}

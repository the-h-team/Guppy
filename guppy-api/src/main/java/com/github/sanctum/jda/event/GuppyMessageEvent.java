package com.github.sanctum.jda.event;

import com.github.sanctum.jda.common.Guppy;
import org.jetbrains.annotations.NotNull;

public abstract class GuppyMessageEvent extends GuppyEvent {
	final Guppy.Message message;

	public GuppyMessageEvent(@NotNull Guppy guppy, @NotNull Guppy.Message message) {
		super(guppy);
		this.message = message;
	}

	public GuppyMessageEvent(@NotNull Guppy guppy, @NotNull Guppy.Message message, @NotNull State state) {
		super(guppy, state);
		this.message = message;
	}

	public @NotNull Guppy.Message getMessage() {
		return message;
	}

	public boolean isContext() {
		return false;
	}

}

package com.github.sanctum.jda.event;

import com.github.sanctum.jda.GuppyAPI;
import com.github.sanctum.jda.common.Guppy;
import com.github.sanctum.panther.event.Vent;
import org.jetbrains.annotations.NotNull;

public abstract class GuppyEvent extends Vent {
	final Guppy guppy;

	public GuppyEvent(@NotNull Guppy guppy) {
		super(GuppyAPI.getInstance().getHost(), State.IMMUTABLE, true);
		this.guppy = guppy;
	}

	public GuppyEvent(@NotNull Guppy guppy, @NotNull State state) {
		super(GuppyAPI.getInstance().getHost(), state, true);
		this.guppy = guppy;
	}

	public @NotNull Guppy getGuppy() {
		return guppy;
	}

}

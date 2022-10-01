package com.github.sanctum.jda.common.util;

import com.github.sanctum.jda.event.GuppyEvent;
import com.github.sanctum.panther.event.Vent;
import java.util.concurrent.CompletableFuture;
import org.jetbrains.annotations.NotNull;

public class GuppyVentCall<T extends GuppyEvent> extends Vent.Call<T> {
	public GuppyVentCall(@NotNull T event) {
		super(event);
	}

	public CompletableFuture<T> schedule() {
		return CompletableFuture.supplyAsync(this::run);
	}

}

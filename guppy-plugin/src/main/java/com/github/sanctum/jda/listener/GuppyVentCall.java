package com.github.sanctum.jda.listener;

import com.github.sanctum.panther.event.Vent;
import java.util.concurrent.CompletableFuture;
import org.jetbrains.annotations.NotNull;

public class GuppyVentCall<T extends Vent> extends Vent.Call<T> {
	public GuppyVentCall(@NotNull T event) {
		super(event);
	}

	public CompletableFuture<T> schedule() {
		return CompletableFuture.supplyAsync(this::run);
	}

}

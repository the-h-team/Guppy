package com.github.sanctum.jda.event;

import com.github.sanctum.jda.common.EphemeralResponse;
import com.github.sanctum.jda.common.Guppy;
import org.jetbrains.annotations.NotNull;

public class GuppyContextActionEvent extends GuppyEvent {

	final String prompt;
	final Guppy target;
	EphemeralResponse response;

	public GuppyContextActionEvent(@NotNull Guppy guppy, @NotNull Guppy target, @NotNull EphemeralResponse response, @NotNull String prompt) {
		super(guppy);
		this.target = target;
		this.prompt = prompt;
		this.response = response;
	}

	public @NotNull Guppy getTarget() {
		return target;
	}

	public @NotNull String getPrompt() {
		return prompt;
	}

	public @NotNull EphemeralResponse getResponse() {
		return response;
	}

	public void setResponse(EphemeralResponse response) {
		this.response = response;
	}
}

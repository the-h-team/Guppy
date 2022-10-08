package com.github.sanctum.jda.event;

import com.github.sanctum.jda.common.EphemeralResponse;
import com.github.sanctum.jda.common.Guppy;
import org.jetbrains.annotations.NotNull;

public class GuppyMessageContextActionEvent extends GuppyMessageEvent {

	final String prompt;
	EphemeralResponse response;
	public GuppyMessageContextActionEvent(@NotNull Guppy guppy, @NotNull Guppy.Message message, @NotNull EphemeralResponse response, @NotNull String prompt) {
		super(guppy, message);
		this.prompt = prompt;
		this.response = response;
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

	@Override
	public boolean isContext() {
		return true;
	}
}

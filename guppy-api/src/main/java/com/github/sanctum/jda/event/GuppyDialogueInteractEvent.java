package com.github.sanctum.jda.event;

import com.github.sanctum.jda.common.Dialogue;
import com.github.sanctum.jda.common.EphemeralResponse;
import com.github.sanctum.jda.common.Guppy;
import org.jetbrains.annotations.NotNull;

public class GuppyDialogueInteractEvent extends GuppyEvent {
	final Dialogue dialogue;
	EphemeralResponse response;
	public GuppyDialogueInteractEvent(@NotNull Guppy guppy, @NotNull Dialogue dialogue) {
		super(guppy);
		this.dialogue = dialogue;
		this.response = () -> "I.. don't know what to say. I wasn't given any direction.";
	}

	public Dialogue getDialogue() {
		return dialogue;
	}

	public EphemeralResponse getResponse() {
		return response;
	}

	public void setResponse(@NotNull EphemeralResponse response) {
		this.response = response;
	}
}

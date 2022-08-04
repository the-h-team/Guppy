package com.github.sanctum.jda.event;

import com.github.sanctum.jda.common.Guppy;
import com.github.sanctum.jda.common.Reaction;
import org.jetbrains.annotations.NotNull;

public class GuppyMessageReactEvent extends GuppyMessageEvent {
	final ReactionResult result;
	final Reaction reaction;

	public GuppyMessageReactEvent(@NotNull Guppy guppy, Guppy.@NotNull Message message, @NotNull String code, @NotNull ReactionResult result) {
		super(guppy, message, State.CANCELLABLE);
		this.result = result;
		this.reaction = message.getReaction(code);
	}

	public Reaction getReaction() {
		return reaction;
	}

	public ReactionResult getResult() {
		return result;
	}

	public enum ReactionResult {
		ADD,
		REMOVE,
	}

}

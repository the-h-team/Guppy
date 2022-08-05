package com.github.sanctum.jda.common;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface EphemeralResponse {

	EphemeralResponse EMPTY = () -> "";

	@NotNull String get();

	default @Nullable EmbeddedMessage getExtra() {
		return null;
	}

	default @NotNull EphemeralResponse setNegated(boolean negated) {
		return this;
	}

	default boolean isNegated() {return false;}

	static @NotNull EphemeralResponse ofNegated(@NotNull String text) {
		EphemeralResponse response = new EphemeralResponse() {
			boolean negated;

			@Override
			public @NotNull String get() {
				return text;
			}

			@Override
			public @NotNull EphemeralResponse setNegated(boolean negated) {
				this.negated = negated;
				return this;
			}

			@Override
			public boolean isNegated() {
				return negated;
			}
		};
		return response.setNegated(true);
	}

}

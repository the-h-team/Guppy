package com.github.sanctum.jda.common;

import com.github.sanctum.panther.annotation.See;
import org.jetbrains.annotations.NotNull;

public interface Reaction {

	@NotNull Object get();

	long count();

	void remove(@NotNull Guppy guppy);

	@NotNull Guppy[] getGuppies();

	static @NotNull Reaction of(@See({String.class, Emoji.class}) @NotNull Object emoji) {
		return new Reaction() {
			@Override
			public @NotNull Object get() {
				return emoji;
			}

			@Override
			public long count() {
				return 0;
			}

			@Override
			public void remove(@NotNull Guppy guppy) {

			}

			@Override
			public @NotNull Guppy[] getGuppies() {
				return new Guppy[0];
			}
		};
	}

	static @NotNull Reaction of(@See({String.class, Emoji.class}) @NotNull Object emoji, long initial) {
		return new Reaction() {
			@Override
			public @NotNull Object get() {
				return emoji;
			}

			@Override
			public long count() {
				return initial;
			}

			@Override
			public void remove(@NotNull Guppy guppy) {

			}

			@Override
			public @NotNull Guppy[] getGuppies() {
				return new Guppy[0];
			}
		};
	}

}

package com.github.sanctum.jda.common;

import com.github.sanctum.panther.container.PantherCollection;
import com.github.sanctum.panther.util.TypeAdapter;
import org.jetbrains.annotations.NotNull;

public interface Command {

	@NotNull String getLabel();

	@NotNull String getDescription();

	@NotNull Options getOptions();

	@NotNull EphemeralResponse onExecuted(@NotNull Guppy guppy, @NotNull Variable variable);

	default void onPreProcess(@NotNull Guppy guppy, @NotNull String[] args) {
	}

	interface Variable {

		@NotNull <T> T get(@NotNull TypeAdapter<T> typeFlag);

		@NotNull <T> T get(@NotNull Class<T> typeFlag);

		<T> boolean contains(@NotNull TypeAdapter<T> typeFlag);

		<T> boolean contains(@NotNull Class<T> typeFlag);

		boolean isEmpty();

	}

	interface Options {

		@NotNull Options add(@NotNull Option... option);

		@NotNull PantherCollection<Option> get();

	}

	interface Option extends Nameable {

		@NotNull String getDescription();

		@NotNull Type getType();

		static @NotNull Option of(@NotNull Type type, @NotNull String name, @NotNull String description) {
			return new Option() {
				@Override
				public @NotNull String getDescription() {
					return description;
				}

				@Override
				public @NotNull String getName() {
					return name;
				}

				@Override
				public @NotNull Type getType() {
					return type;
				}
			};
		}

		enum Type {
			USER,
			INTEGER,
			NUMBER,
			ROLE,
			STRING
		}


	}


}

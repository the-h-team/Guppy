package com.github.sanctum.jda.common;

import com.github.sanctum.panther.container.PantherCollection;
import com.github.sanctum.panther.util.TypeAdapter;
import org.jetbrains.annotations.NotNull;

public interface Command {

	default @NotNull String getName() {
		return getLabel();
	}

	default @NotNull String getLabel() {
		return "mycommand";
	}

	default @NotNull String getDescription() {
		return "my command description";
	}

	default @NotNull String getMessage() {
		return getLabel();
	}

	default @NotNull Options getOptions() {
		return new Options() {
			@Override
			public @NotNull Options add(@NotNull Option... option) {
				return null;
			}

			@Override
			public @NotNull PantherCollection<Option> get() {
				return null;
			}
		};
	}

	default @NotNull Type getType() {
		return Type.SLASH;
	}

	default void onProcess(@NotNull Guppy guppy, @NotNull String[] args) {
	}

	default @NotNull EphemeralResponse onExecuted(@NotNull Guppy guppy, @NotNull Variable variable) {
		return EphemeralResponse.EMPTY;
	}

	default @NotNull EphemeralResponse onContext(@NotNull Guppy guppy, @NotNull Command.ContextVariable variable) {
		return EphemeralResponse.EMPTY;
	}

	enum Type {
		/**
		 * Signifies a user only context menu command.
		 */
		USER,
		/**
		 * Signifies a message only context menu command.
		 */
		MESSAGE,
		/**
		 * Signifies a slash command only.
		 */
		SLASH,
		/**
		 * Signifies either a user only context menu command or slash command.
		 */
		MULTI_USER,
		/**
		 * Signifies either a message only context menu command or slash command.
		 */
		MULTI_MESSAGE,
		UNKNOWN;
	}

	interface ContextVariable {

		Guppy.Message getAsMessage();

		Guppy getAsGuppy();

	}

	interface Variable {

		@NotNull Channel getChannel();

		@NotNull <T> T get(@NotNull TypeAdapter<T> typeFlag, int index);

		@NotNull <T> T get(@NotNull Class<T> typeFlag, int index);

		<T> boolean contains(@NotNull TypeAdapter<T> typeFlag);

		<T> boolean contains(@NotNull Class<T> typeFlag);

		<T> int size(@NotNull Class<T> typeFlag);

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

package com.github.sanctum.jda.common;

import com.github.sanctum.panther.container.ImmutablePantherCollection;
import com.github.sanctum.panther.container.PantherCollection;
import org.jetbrains.annotations.NotNull;

public abstract class JDACommand implements Command {

	final String label;
	final String description;
	final Options options;

	protected JDACommand(@NotNull String label, @NotNull String description) {
		this.label = label;
		this.description = description;
		this.options = new Options() {
			final ImmutablePantherCollection.Builder<Option> options = ImmutablePantherCollection.builder();

			@Override
			public @NotNull Options add(@NotNull Option... option) {
				for (Option o : option) {
					this.options.add(o);
				}
				return this;
			}

			@Override
			public @NotNull PantherCollection<Option> get() {
				return options.build();
			}
		};
	}

	@Override
	public @NotNull String getLabel() {
		return this.label;
	}

	@Override
	public @NotNull String getDescription() {
		return this.description;
	}

	@Override
	public @NotNull Options getOptions() {
		return this.options;
	}

	@Override
	public abstract @NotNull EphemeralResponse onExecuted(@NotNull Guppy guppy, @NotNull Variable variable);

}

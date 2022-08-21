package com.github.sanctum.jda.common;

import com.github.sanctum.panther.container.PantherCollection;
import com.github.sanctum.panther.util.Deployable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface CommandController {

	/**
	 * Get a command by its label.
	 *
	 * @param label The name of the command.
	 * @return A command if found or null.
	 */
	@Nullable Command get(@NotNull String label);

	/**
	 * @return a collection of registered commands.
	 */
	@NotNull PantherCollection<Command> getAll();

	/**
	 * @param command
	 * @return
	 */
	@NotNull Deployable<Void> add(@NotNull Command command);

	/**
	 * @param command
	 * @return
	 */
	@NotNull Deployable<Void> remove(@NotNull Command command);

	/**
	 * @return a deployable sequence that refreshes the command map.
	 */
	@NotNull Deployable<Void> refresh();

}

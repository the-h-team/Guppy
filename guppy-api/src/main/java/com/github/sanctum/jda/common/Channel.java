package com.github.sanctum.jda.common;

import com.github.sanctum.panther.container.PantherCollection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An object responsible for handling text channel and private channel delegation.
 */
public interface Channel extends Identifiable, Mailable {

	@Nullable Thread getThread(@NotNull String name);

	@Nullable Thread getThread(long id);

	@NotNull PantherCollection<Thread> getThreads();

	boolean isPrivate();

	void delete();

	interface Thread extends Identifiable, Mailable {

		boolean isOwned();

		@Nullable Guppy getOwner();

		@NotNull Channel getParent();

		void delete();

	}

}

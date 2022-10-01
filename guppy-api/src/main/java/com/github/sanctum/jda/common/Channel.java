package com.github.sanctum.jda.common;

import com.github.sanctum.panther.container.PantherCollection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An object responsible for delegating all channel types.
 */
public interface Channel extends Connectable, Identifiable, Mailable {

	@Nullable Thread getThread(@NotNull String name);

	@Nullable Thread getThread(long id);

	@NotNull PantherCollection<Thread> getThreads();

	@Nullable Guppy.Message getMessage(long id);

	@NotNull PantherCollection<Guppy.Message> getHistory();

	boolean isPrivate();

	boolean isNews();

	boolean isVoice();

	void setName(@NotNull String newName);

	void delete();

	interface Thread extends Identifiable, Mailable {

		boolean isOwned();

		@Nullable Guppy getOwner();

		@NotNull Channel getParent();

		void delete();

	}

}

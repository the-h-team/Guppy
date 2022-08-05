package com.github.sanctum.jda.common;


import com.github.sanctum.panther.util.Deployable;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Guppy extends Identifiable, Mailable {

	@NotNull String getTag();

	@NotNull String getAsMention();

	@NotNull String getAvatarUrl();

	@Nullable Link getLink();

	@NotNull Role[] getRoles();

	@Nullable Role getRole(@NotNull String name);

	@Nullable Role getRole(long id);

	interface Message {

		@NotNull String getText();

		@Nullable Channel getChannel();

		@Nullable Channel.Thread getThread();

		@NotNull Reaction[] getReactions();

		@NotNull EmbeddedMessage[] getAttached();

		@Nullable Reaction getReaction(@NotNull String code);

		void add(@NotNull Reaction reaction);

		void take(@NotNull Reaction reaction);

		void delete();

		default boolean isFromThread() {
			return getThread() != null;
		}

		default boolean isEmbedded() {
			return getAttached().length > 0;
		}

	}

	abstract class Link implements Nameable {

		public abstract @NotNull String getName();

		public abstract @NotNull UUID getId();

		public abstract Deployable<Void> sendMessage(@NotNull Object... components);
	}

}

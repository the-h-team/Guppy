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

	@NotNull Voice getVoice();

	@NotNull Role[] getRoles();

	@Nullable Role getRole(@NotNull String name);

	@Nullable Role getRole(long id);

	boolean has(@NotNull Role... roles);

	void inherit(@NotNull Role... roles);

	void revoke(@NotNull Role... roles);

	Deployable<Void> setLink(@NotNull Link link);

	interface Message {

		@NotNull String getText();

		long getId();

		@Nullable Guppy getAuthor();

		@Nullable Channel getChannel();

		@Nullable Channel.Thread getThread();

		@NotNull Reaction[] getReactions();

		@NotNull EmbeddedMessage[] getAttached();

		@Nullable Reaction getReaction(@NotNull String code);

		int getTotalReactions();

		boolean hasMedia();

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

	interface Voice {

		boolean isSelfMuted();

		boolean isSelfDeafened();

		boolean isMuted();

		boolean isDeafened();

		boolean setMuted(boolean muted);

		default boolean isActive() {
			return getChannel() != null && getChannel().isVoice();
		}

		@Nullable Channel getChannel();

	}

	abstract class Link implements Nameable {

		public abstract @NotNull UUID getId();

		public abstract Deployable<Void> sendMessage(@NotNull Object... components);



	}

}

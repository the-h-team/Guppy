package com.github.sanctum.jda.common;

import org.jetbrains.annotations.NotNull;

public interface Role extends Identifiable, Mailable {

	@NotNull Permission[] getPermissions();

	void permit(@NotNull Permission... permissions);

	void revoke(@NotNull Permission... permissions);

	interface Attachment {

		@NotNull Role getRole();

		@NotNull Permission[] getPermissions();

	}

}

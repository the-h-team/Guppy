package com.github.sanctum.jda.common;

import org.jetbrains.annotations.NotNull;

public interface Role extends Identifiable, Mailable {

	@NotNull Permission[] getPermissions();

	interface Attachment {

		@NotNull Role getRole();

		@NotNull Permission[] getPermissions();

	}

}

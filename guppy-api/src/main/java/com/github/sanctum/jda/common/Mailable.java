package com.github.sanctum.jda.common;

import com.github.sanctum.panther.util.Deployable;
import org.jetbrains.annotations.NotNull;

/**
 * An object that can handle direct messaging.
 */
public interface Mailable {

	Deployable<Guppy.Message> sendMessage(@NotNull String message);

}

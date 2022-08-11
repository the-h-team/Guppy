package com.github.sanctum.jda.common;

import com.github.sanctum.panther.util.Deployable;

/**
 * An object that can handle direct messaging.
 */
public interface Connectable {

	Deployable<Void> open();

	Deployable<Void> close();

}

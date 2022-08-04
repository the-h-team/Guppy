package com.github.sanctum.jda.common;


import com.github.sanctum.panther.util.Deployable;

public interface JDAController {

	boolean isRunning();

	Deployable<JDAController> stop();

	Deployable<JDAController> start();

	Deployable<JDAController> restart();

	Deployable<JDAController> restartAfter(long wait);

}

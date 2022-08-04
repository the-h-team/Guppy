package com.github.sanctum.jda.ui.util;

import java.util.Timer;
import java.util.TimerTask;
import javax.swing.*;
import org.jetbrains.annotations.NotNull;

public final class TaskScheduler {

	static final Timer timer = new Timer(true);

	public static void now(@NotNull Runnable runnable) {
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				SwingUtilities.invokeLater(runnable);
			}
		}, 0);
	}

	public static void later(@NotNull Runnable runnable, long time) {
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				SwingUtilities.invokeLater(runnable);
			}
		}, time);
	}

}

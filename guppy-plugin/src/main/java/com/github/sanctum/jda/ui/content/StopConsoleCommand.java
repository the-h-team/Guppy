package com.github.sanctum.jda.ui.content;

import com.github.sanctum.jda.GuppyEntryPoint;
import com.github.sanctum.jda.ui.api.ConsoleCommand;
import com.github.sanctum.panther.container.PantherArrays;

public class StopConsoleCommand extends ConsoleCommand {
	public StopConsoleCommand() {
		super("stop");
		setAliases(PantherArrays.asList("quit", "exit"));
	}

	@Override
	public void execute(String[] arguments) {
		if (arguments.length == 0) {
			GuppyEntryPoint.getInstance().getJDA().shutdown();
			System.exit(0);
		}
	}
}

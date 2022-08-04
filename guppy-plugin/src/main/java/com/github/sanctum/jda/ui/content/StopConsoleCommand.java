package com.github.sanctum.jda.ui.content;

import com.github.sanctum.jda.GuppyAPI;
import com.github.sanctum.jda.GuppyEntryPoint;
import com.github.sanctum.jda.common.Channel;
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
			GuppyEntryPoint.getMainPanel().stop();
			return;
		}
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < arguments.length; i++) {
			if (i == arguments.length - 1) {
				builder.append(arguments[i]);
			} else {
				builder.append(arguments[i]).append(" ");
			}
		}
		Channel c = GuppyAPI.getInstance().getChannel(751691534021165238L);
		if (c != null) c.sendMessage(builder.toString()).queue();
	}
}

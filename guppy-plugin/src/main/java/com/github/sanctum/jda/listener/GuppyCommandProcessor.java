package com.github.sanctum.jda.listener;

import com.github.sanctum.jda.GuppyAPI;
import com.github.sanctum.jda.common.Command;
import com.github.sanctum.jda.common.Guppy;
import com.github.sanctum.jda.event.GuppyMessageSentEvent;
import com.github.sanctum.panther.event.Subscribe;
import com.github.sanctum.panther.util.PantherString;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GuppyCommandProcessor {

	@Subscribe
	public void onMessage(GuppyMessageSentEvent e) {
		if (!e.isToBot()) {
			Guppy.Message message = e.getMessage();
			String text = message.getText();
			if (text.startsWith("/")) {
				Command c = GuppyAPI.getInstance().getCommands().get(text.split(" ")[0].replace("/", ""));
				if (c != null) {
					List<String> list = new ArrayList<>(Arrays.asList(text.split(" ")));
					list.removeIf(s -> new PantherString(s).contains("/" + c.getLabel()));
					c.onProcess(e.getGuppy(), list.toArray(new String[0]));
				}
			}
		}
	}

}

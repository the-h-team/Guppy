package com.github.sanctum.jda.listener;

import com.github.sanctum.jda.GuppyAPI;
import com.github.sanctum.jda.common.Emoji;
import com.github.sanctum.jda.common.EphemeralResponse;
import com.github.sanctum.jda.common.Guppy;
import com.github.sanctum.jda.common.JDACommand;
import com.github.sanctum.jda.common.Reaction;
import org.jetbrains.annotations.NotNull;

public class ExampleCommand extends JDACommand {
	public ExampleCommand() {
		super("bmsg", "a user messaging command.");
		Option user = Option.of(Option.Type.USER, "user", "A user to message.");
		Option word = Option.of(Option.Type.STRING, "message", "The message to send.");
		getOptions().add(user, word);
	}

	@Override
	public void onPreProcess(@NotNull Guppy guppy, @NotNull String[] args) {

	}

	@Override
	public @NotNull EphemeralResponse onExecuted(@NotNull Guppy guppy, @NotNull Variable variable) {
		EphemeralResponse INVALID = () -> "I don't know what you want from me.";
		Class<Guppy> flag = Guppy.class;
		if (variable.contains(flag)) {
			Class<String> sflag = String.class;
			if (variable.contains(sflag)) {
				Guppy g = variable.get(flag, 0);
				String message = variable.get(sflag, 0);
				Guppy.Message m = g.sendMessage(message).submit().join();
				Emoji e = GuppyAPI.getInstance().getEmoji("wat");
				if (e != null) {
					m.add(Reaction.of(e));
				}
				return () -> "Sent message `" + message + "` to " + g.getName();
			}
		}
		return INVALID;
	}
}

package com.github.sanctum.jda;

import com.github.sanctum.jda.common.api.JDAInput;
import com.github.sanctum.jda.common.content.MainPanel;
import java.util.Locale;
import javax.swing.*;
import net.dv8tion.jda.api.entities.Activity;

public final class GuppyLocalApp {

	static MainPanel window;

	public static MainPanel getWindow() {
		return window;
	}

	public static void main(String[] args) {
		// open gui
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(true);
		frame.setUndecorated(true);
		frame.setTitle("Guppy Bot");

		MainPanel m = (window = new MainPanel(frame) {
		});
		m.start();
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);

		m.getConsole().toggleVisibility();

		Activity activity = Activity.watching("Aqua Teen Hunger Force");
		JDAInput input = new JDAInput();
		String test = JOptionPane.showInputDialog("Enter your bot token.");
		if (test == null || test.isEmpty())
		test = JOptionPane.showInputDialog("Bot token required! Please provide one");
		input.setToken(test);
		String[] s = new String[]{"A.) Watching", "B.) Competing", "C.) Playing", "D.) Listening"};
		input.setActivity(s[JOptionPane.showOptionDialog(null,
		"Now select a status type.",
		"Good....",
		JOptionPane.DEFAULT_OPTION,
		JOptionPane.INFORMATION_MESSAGE,
		null,
		s,
		s[0])]);
		input.setActivityMessage(JOptionPane.showInputDialog("Now enter a status message."));

		switch (input.getActivity().toLowerCase(Locale.ROOT)) {
			case "a.) watching":
				activity = Activity.watching(input.getActivityMessage());
				break;
			case "b.) competing":
				activity = Activity.competing(input.getActivityMessage());
				break;
			case "c.) playing":
				activity = Activity.playing(input.getActivityMessage());
				break;
			case "d.) listening":
				activity = Activity.listening(input.getActivityMessage());
				break;
		}

		GuppyEntryPoint.enable(input.getToken(), activity);

	}

}

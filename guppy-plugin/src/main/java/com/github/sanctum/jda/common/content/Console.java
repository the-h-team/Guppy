package com.github.sanctum.jda.common.content;

import com.github.sanctum.jda.GuppyEntryPoint;
import com.github.sanctum.jda.common.util.TextAreaOutputStream;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.PrintStream;
import java.util.Arrays;
import javax.swing.*;

public abstract class Console extends JFrame {
	protected JTextField jt;
	protected JTextArea ta;
	protected JLabel l;
	protected boolean typing;
	protected Timer t;
	protected final MainPanel window;

	public Console(MainPanel window) {
		this.window = window;
		createGUI();
	}

	void createGUI() {

		// Set frame properties
		setTitle("Console");

		JPanel jp = new JPanel();
		// Set layout
		jp.setLayout(new GridLayout(2, 1));
		l = new JLabel();
		jp.add(l);

		// Create a timer that executes every 1 millisecond
		t = new Timer(1, ae -> {
			// If the user isn't typing, he is thinking
			if (!typing)
				l.setText("Thinking..");
		});

		// Set initial delay of 2000 ms
		// That means, actionPerformed() is executed 2500ms
		// after the start() is called
		t.setInitialDelay(2000);

		// Create JTextField, add it.
		jt = new JTextField();
		jt.addKeyListener(window.handler);
		jp.add(jt);


		// Add panel to the south,
		add(jp, BorderLayout.SOUTH);


		// Add a KeyListener
		jt.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent ke) {

				// Key pressed means, the user is typing
				l.setText("You are typing..");

				// When key is pressed, stop the timer
				// so that the user is not thinking, he is typing
				t.stop();

				// He is typing, the key is pressed
				typing = true;

				// If he presses enter, add text to chat textarea
				if (ke.getKeyCode() == KeyEvent.VK_ENTER) runCommand(jt.getText());
				if (ke.getKeyCode() == KeyEvent.VK_ESCAPE) {
					setVisible(false);
					window.stop();
				}

			}

			public void keyReleased(KeyEvent ke) {

				// When the user isn't typing..
				typing = false;

				// If the timer is not running, i.e.
				// when the user is not thinking..
				if (!t.isRunning())

					// He released a key, start the timer,
					// the timer is started after 2500ms, it sees
					// whether the user is still in the keyReleased state
					// which means, he is thinking
					t.start();
			}
		});

		// Create a textarea
		ta = new JTextArea();
		ta.addKeyListener(window.handler);

		// Make it non-editable
		ta.setEditable(false);

		// Set some margin, for the text
		ta.setMargin(new Insets(7, 7, 7, 7));

		// Set a scrollpane
		JScrollPane js = new JScrollPane(ta);
		add(js);

		addWindowListener(new WindowAdapter() {
			public void windowOpened(WindowEvent we) {
				// Get the focus when window is opened
				jt.requestFocus();
			}
		});

		setSize(400, 400);
		setLocationRelativeTo(null);
		setResizable(false);
		setUndecorated(true);
		Color color = UIManager.getColor("activeCaptionBorder");
		getRootPane().setBorder(BorderFactory.createLineBorder(color, 4));
		//setVisible(true);
		addWindowListener(new WindowListener() {
			@Override
			public void windowOpened(WindowEvent e) {

			}

			@Override
			public void windowClosing(WindowEvent e) {
				window.stop();
			}

			@Override
			public void windowClosed(WindowEvent e) {

			}

			@Override
			public void windowIconified(WindowEvent e) {

			}

			@Override
			public void windowDeiconified(WindowEvent e) {

			}

			@Override
			public void windowActivated(WindowEvent e) {

			}

			@Override
			public void windowDeactivated(WindowEvent e) {

			}
		});
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentMoved(ComponentEvent e) {

				window.setLocation(getX(), getY());

			}
		});
		PrintStream con = new PrintStream(new TextAreaOutputStream(ta));
		// Set up the output stream for our console printing.
		System.setOut(con);
		System.setErr(con);
	}

	public MainPanel getPanel() {
		return this.window;
	}

	public void toggleVisibility() {
		setVisible(!isVisible());
	}

	public void sendMessage(String text) {
		// If text is empty return
		if (text.trim().isEmpty()) return;

		// Otherwise, append text with a new line
		ta.append(text + "\n");

		// Set textfield and label text to empty string
		jt.setText("");
		l.setText("");
	}

	public void runCommand(String text) {
		// If text is empty return
		if (text.trim().isEmpty()) return;

		String label = text.replace("/", "").split(" ")[0];
		GuppyEntryPoint.getConsoleCommands().forEach(cmd -> {
			if (cmd.getLabel().equalsIgnoreCase(label) || cmd.getAliases().stream().anyMatch(label::equalsIgnoreCase))
				cmd.execute(Arrays.stream(text.replace("/", "").split(" ")).filter(s -> !s.equalsIgnoreCase(cmd.getLabel()) && cmd.getAliases().stream().noneMatch(s::equalsIgnoreCase)).toArray(String[]::new));
		});

		// Set textfield and label text to empty string
		jt.setText("");
		l.setText("");
	}

}
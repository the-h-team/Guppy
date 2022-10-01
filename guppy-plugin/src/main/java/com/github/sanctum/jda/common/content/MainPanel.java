package com.github.sanctum.jda.common.content;

import static javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE;

import com.github.sanctum.panther.container.PantherEntryMap;
import com.github.sanctum.panther.container.PantherMap;
import com.github.sanctum.panther.util.TaskChain;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.*;
import net.dv8tion.jda.api.JDA;

public abstract class MainPanel extends JPanel implements Runnable {

	PantherMap<Integer, Integer> infoCounterMap = new PantherEntryMap<>();
	PantherMap<Integer, String> infoMap = new PantherEntryMap<>();
	PantherMap<Integer, Integer> infoXMap = new PantherEntryMap<>();
	PantherMap<Integer, Integer> infoYMap = new PantherEntryMap<>();

	Image image, esc;
	Font font;
	Thread local;
	JDA jda;

	final JFrame frame;
	final ConsoleSetup inputConsole;
	final Console console;
	final KeyHandler handler;

	public MainPanel(JFrame frame) {
		this.frame = frame;
		this.console = new Console(this){};
		this.inputConsole = new ConsoleSetup(this);
		this.handler = new KeyHandler(this);
		frame.add(this);
		setPreferredSize(new Dimension(1280, 720));
		setDoubleBuffered(true);
		setFocusable(true);
		addKeyListener(handler);
		try {
			this.font = Font.createFont(Font.TRUETYPE_FONT, MainPanel.class.getResourceAsStream("/fonts/Code.otf"));
			this.image = ImageIO.read(MainPanel.class.getResourceAsStream("/backgrounds/15542.jpg"));
			this.esc = ImageIO.read(MainPanel.class.getResourceAsStream("/images/escape.png"));
		} catch (IOException | FontFormatException e) {
			e.printStackTrace();
		}
	}

	public Console getConsole() {
		return console;
	}

	public ConsoleSetup getInputConsole() {
		return inputConsole;
	}

	public JDA getJda() {
		return jda;
	}

	public void setJda(JDA jda) {
		this.jda = jda;
	}

	public void start() {
		if (local != null) return;
		local = new Thread(this);
		local.start();
	}

	public void run() {
		while (local != null) {
			repaint();
		}
	}

	public void displayInfo(int key, int x, int y, String info) {
		infoCounterMap.put(key, 0);
		infoMap.put(key, info);
		infoXMap.put(key, x);
		infoYMap.put(key, y);
	}

	public void stop() {
		int res = JOptionPane.showConfirmDialog(this, "Are you sure you want to close?", "Close?", JOptionPane.YES_NO_OPTION);
		if (res == 1) {
			// dispose method issues the WINDOW_CLOSED event
			console.setVisible(true);
			frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		} else {
			if (jda != null) {
				jda.shutdown();
				TaskChain.getAsynchronous().wait(() -> {
					frame.dispose();
					System.exit(0);
					frame.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
				}, 1000);
			}
		}
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		g.drawImage(image, getX(), getY(), null);

		g.setFont(font.deriveFont(30F));

		g.setColor(Color.WHITE);

		g.drawString("Press", 60, 580);

		g.drawImage(esc.getScaledInstance(80, 80, 0), 60, 580, null);

		g.drawString("To Exit", 45, 680);
		infoMap.forEach(entry -> {
			int count = infoCounterMap.get(entry.getKey());
			if (count == 4500) {
				TaskChain.getAsynchronous().run(() -> {
					infoCounterMap.remove(entry.getKey());
					infoMap.remove(entry.getKey());
					infoYMap.remove(entry.getKey());
					infoXMap.remove(entry.getKey());
				});
			} else {
				infoCounterMap.put(entry.getKey(), count + 1);
				g.setColor(Color.ORANGE);
				g.drawString(entry.getValue(), infoXMap.get(entry.getKey()), infoYMap.get(entry.getKey()));
			}
		});

	}

	public static class KeyHandler implements KeyListener {

		final MainPanel instance;

		KeyHandler(MainPanel window) {
			this.instance = window;
		}

		@Override
		public void keyTyped(KeyEvent e) {

		}

		@Override
		public void keyPressed(KeyEvent e) {

			int code = e.getKeyCode();

			if (code == KeyEvent.VK_T) {
				instance.getConsole().toggleVisibility();
			}

			if (code == KeyEvent.VK_ESCAPE) {
				if (instance.getConsole().isVisible()) {
					instance.getConsole().setVisible(false);
				}
				instance.stop();
			}

		}

		@Override
		public void keyReleased(KeyEvent e) {

		}
	}
}

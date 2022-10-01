package com.github.sanctum.jda.common.util;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.*;

public class ImageScaler {

	public static ImageIcon scale(ImageIcon icon, int height, int width) {
		return new ImageIcon(icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH));
	}

	public static BufferedImage of(BufferedImage original, int width, int height) {
		BufferedImage copy = new BufferedImage(width, height, original.getType());
		Graphics2D g = copy.createGraphics();
		g.drawImage(original, 0, 0, width, height, null);
		g.dispose();
		return copy;
	}

}

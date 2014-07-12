package org.ukiuni.report.util;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.imageio.ImageIO;

public class ImageUtil {
	public void trim(InputStream in, int width, int height, OutputStream out) throws IOException {
		BufferedImage image = ImageIO.read(in);
		double widthScale = (double) width / (double) image.getWidth();
		double heightScale = (double) height / (double) image.getHeight();
		double scale = widthScale > heightScale ? widthScale : heightScale;
		int scaledWidth = (int) (image.getWidth() * scale);
		int scaledHeight = (int) (image.getHeight() * scale);
		BufferedImage thumb = new BufferedImage(scaledWidth, scaledHeight, image.getType());
		thumb.getGraphics().drawImage(image.getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_SMOOTH), 0, 0, scaledWidth, scaledHeight, null);
		ImageIO.write(thumb, "PNG", out);
	}
}

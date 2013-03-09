package de.samueltufan.qrdecoder;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class BufferedImageUtils
{
	public static BufferedImage readBufferedImage(String filePath)
	{
		File f = new File(filePath);

		BufferedImage image = null;

		try
		{
			image = ImageIO.read(f);
		}
		catch (IOException e1)
		{
			return null;
		}

		return image;
	}

	public static void writeBufferedImage(BufferedImage image, String filePath)
	{
		File f = new File(filePath);
		try
		{
			ImageIO.write(image, "PNG", f);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}

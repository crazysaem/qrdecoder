package de.samueltufan.qrdecoder;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class LocateQrCode 
{
	private static int[][] pixeColors;
	
	private static final int BLACK = 0;
	private static final int WHITE = 1;
	private static final int VOID = 2;
	
	private static final boolean DEBUG = true;
	
	private static BufferedImage debugImage;	
	private static File f;
	
	public static BufferedImage locate (BufferedImage image, String name)
	{
		int 	color, count = 0, countWhiteX = 0, countBlackX = 0, countWhiteY = 0, countBlackY = 0, countDiffX = 0, countDiffY = 0,
				fuziness = 10;
		
		pixeColors = convertTo2DWithoutUsingGetRGB(image);
		Graphics g;
		
		if (DEBUG)
		{
			f = new File("src/data/" + name + "debug.png");
			
			debugImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
			g = debugImage.getGraphics();
			g.setColor(Color.WHITE);
			g.clearRect(0, 0, image.getWidth(), image.getHeight());
			g.drawRect(0, 0, image.getWidth(), image.getHeight());
			g.setColor(Color.RED);
		}
		
		for (int y = 0; y < pixeColors[0].length; y++)
		{
			count = 0;
			
			for (int x = 0; x < pixeColors.length; x++)
			{					
				color = getColor(x, y);
				if (color == WHITE)
				{
					count++;
				}
				else
				{
					if (count>15)
					{
						countWhiteX = count;	
						
						//check for a corresponding black line above the white one
						countBlackX = colorCountX(x - countWhiteX, y - fuziness, x, y, BLACK);						
						countDiffX = Math.abs(countBlackX - countWhiteX);
						
						if (countDiffX < 5)
						{
							//check for a corresponding white and black line vertically below from this point
							countWhiteY = colorCountY(x - 2, y, x, y + countWhiteX, WHITE);
							countBlackY = colorCountY(x + 2, y, x + fuziness, y + countWhiteX, BLACK);
							
							countDiffY = Math.abs(countBlackY - countWhiteY);
							
							if (countDiffY < 5)
							{
								if (Math.abs(countWhiteX - countWhiteY) < 5)
								{
									System.out.println("top right corner! x: " + x + " y: " + y + " width: " + countWhiteX + " height: " + countWhiteY);
									if (DEBUG)
									{
										g.drawLine(x - countWhiteX, y, x, y + 1);
										g.drawLine(x, y, x + 1, y + countWhiteY);
									}
								}								
							}							
						}
						else
						{
							//check for a corresponding black line below the white one
							countBlackX = colorCountX(x - countWhiteX, y, x, y + fuziness, BLACK);						
							countDiffX = Math.abs(countBlackX - countWhiteX);
							
							if (countDiffX < 5)
							{
								//check for a corresponding white and black line vertically above from this point
								countWhiteY = colorCountY(x - 2, y - countWhiteX, x, y, WHITE);
								countBlackY = colorCountY(x + 2, y - countWhiteX, x + fuziness, y, BLACK);
								
								countDiffY = Math.abs(countBlackY - countWhiteY);
								
								if (countDiffY < 5)
								{
									if (Math.abs(countWhiteX - countWhiteY) < 5)
									{
										System.out.println("bottom right corner! x: " + x + " y: " + y + " width: " + countWhiteX + " height: " + countWhiteY);
										if (DEBUG)
										{
											g.drawLine(x - countWhiteX, y, x, y + 1);
											g.drawLine(x, y - countWhiteY, x + 1, y);
										}
									}								
								}							
							}
						}
					}
					
					count = 0;
				}
			}
		}
		
		if (DEBUG)
		{
			g.dispose();
			
			try 
			{
				ImageIO.write(debugImage, "PNG", f);
			} catch (IOException e) 
			{
				e.printStackTrace();
			}
		}
		
		return image;	
	}
	
	public static int colorCountX (int xstart, int ystart, int xend, int yend, int color)
	{
		int count = 0, countMax = 0;
		
		for (int y = ystart; y <= yend; y++)
		{	
			count = 0;
			
			for (int x = xstart; x <= xend; x++)
			{				
				if (getColor(x, y) == color)
				{
					count++;
				}
			}
			
			countMax = Math.max(countMax, count);
		}
		
		return countMax;
	}
	
	public static int colorCountY (int xstart, int ystart, int xend, int yend, int color)
	{
		int count = 0, countMax = 0;
		
		for (int x = xstart; x <= xend; x++)		
		{	
			count = 0;
			
			for (int y = ystart; y <= yend; y++)
			{				
				if (getColor(x, y) == color)
				{
					count++;
				}
			}
			
			countMax = Math.max(countMax, count);
		}
		
		return countMax;
	}
	
	private static int getColor(int x, int y)
	{
		if (x < 0 || y < 0 || x >= pixeColors.length || y >= pixeColors[0].length)
		{
			return VOID;
		}
		
		int r, g, b;
		
		r = (((int) pixeColors[x][y] & 0xff0000) >> 16); // red
		g = (((int) pixeColors[x][y] & 0xff00) >> 8); // green
		b = ((int) pixeColors[x][y] & 0xff); // blue	
		
		if (getBrightness(r, g, b) > 0.6)
		{
			return WHITE;
		}
		
		if (getBrightness(r, g, b) < 0.3)
		{
			return BLACK;
		}
		
		return VOID;
	}
	
	private static float getBrightness(int r, int g, int b)
	{
		float brightness;	
		
		int cmax = (r > g) ? r : g;
		if (b > cmax) cmax = b;
		
		brightness = ((float) cmax) / 255.0f;

		return brightness;
	}
	
	private static int[][] convertTo2DWithoutUsingGetRGB(BufferedImage image) 
	{
	      final byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
	      final int width = image.getWidth();
	      final int height = image.getHeight();
	      final boolean hasAlphaChannel = image.getAlphaRaster() != null;

	      int[][] result = new int[width][height];
	      if (hasAlphaChannel) 
	      {
	         final int pixelLength = 4;
	         for (int pixel = 0, row = 0, col = 0; pixel < pixels.length; pixel += pixelLength) {
	            int argb = 0;
	            argb += (((int) pixels[pixel] & 0xff) << 24); // alpha
	            argb += ((int) pixels[pixel + 1] & 0xff); // blue
	            argb += (((int) pixels[pixel + 2] & 0xff) << 8); // green
	            argb += (((int) pixels[pixel + 3] & 0xff) << 16); // red
	            result[col][row] = argb;
	            col++;
	            if (col == width) 
	            {
	               col = 0;
	               row++;
	            }
	         }
	      } 
	      else 
	      {
	         final int pixelLength = 3;
	         for (int pixel = 0, row = 0, col = 0; pixel < pixels.length; pixel += pixelLength) 
	         {
	            int argb = 0;
	            //argb += -16777216; // 255 alpha
	            argb += ((int) pixels[pixel] & 0xff); // blue
	            argb += (((int) pixels[pixel + 1] & 0xff) << 8); // green
	            argb += (((int) pixels[pixel + 2] & 0xff) << 16); // red
	            //result[row][col] = argb;
	            result[col][row] = argb;
	            col++;
	            if (col == width) 
	            {
	               col = 0;
	               row++;
	            }
	         }
	      }

	      return result;
	   }
}

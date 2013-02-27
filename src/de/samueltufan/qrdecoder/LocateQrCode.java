package de.samueltufan.qrdecoder;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

public class LocateQrCode 
{
	private static int[][] pixeColors;
	
	private static final int BLACK = 0;
	private static final int WHITE = 1;
	private static final int VOID = 2;
	
	public static BufferedImage locate (BufferedImage image)
	{
		int color, count = 0, countWhiteX = 0, countBlackX = 0, countWhiteY = 0, countBlackY = 0, countDiffX = 0, countDiffY = 0;
		
		pixeColors = convertTo2DWithoutUsingGetRGB(image);
		
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
						countBlackX = colorCountX(x - countWhiteX, y - 5, x, y, BLACK);
						
						countDiffX = Math.abs(countBlackX - countWhiteX);
						
						if (countDiffX < 5)
						{
							countWhiteY = colorCountY(x - 2, y, x, y + countWhiteX, WHITE);
							countBlackY = colorCountY(x + 2, y, x + 5, y + countWhiteX, BLACK);
							
							countDiffY = Math.abs(countBlackY - countWhiteY);
							
							if (countDiffY < 5)
							{
								if (Math.abs(countDiffX - countDiffY) < 5)
								{
									System.out.println("x: " + x + " y: " + y);
								}								
							}							
						}	
					}
					
					count = 0;
				}
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

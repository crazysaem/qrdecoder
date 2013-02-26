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
		pixeColors = convertTo2DWithoutUsingGetRGB(image);
		
		for (int y = 0; y < pixeColors.length; y++)
		{
			for (int x = 0; x < pixeColors[y].length; x++)
			{
				if (getColor(x, y) == BLACK)
				{
					if (checkIfQrPart(x, y))
					{
						System.out.println("found! x: " + x + " y: " + y);
					}
				}				
			}
		};
		
		return image;	
	}
	
	private static boolean checkIfQrPart(int x, int y)
	{
		int leftStatus = 0, rightStatus = 0, bottomStatus = 0, topStatus = 0, color;
		int diffMax = 250;
		
		for (int diff = 0; diff <= diffMax; diff++)
		{			
			//left:	
			color = getColor(x - diff, y);
			
			switch (color)
			{
				case WHITE:
					if (leftStatus % 2 == 0)
					{
						leftStatus++;
					}
				break;
				
				case BLACK:
					if (leftStatus % 2 == 1)
					{
						leftStatus++;
					}
				break;
				
				case VOID:
					if (leftStatus<2)
					{
						leftStatus = -1;
						diff = diffMax;
					}
				break;
			}
					
			//right:
			color = getColor(x + diff, y);
			
			switch (color)
			{
				case WHITE:
					if (rightStatus % 2 == 0)
					{
						rightStatus++;
					}
				break;
				
				case BLACK:
					if (rightStatus % 2 == 1)
					{
						rightStatus++;
					}
				break;
				
				case VOID:
					if (rightStatus<2)
					{
						rightStatus = -1;
						diff = diffMax;
					}
				break;
			}
					
			//bottom:				
			color = getColor(x, y + diff);
			
			switch (color)
			{
				case WHITE:
					if (bottomStatus % 2 == 0)
					{
						bottomStatus++;
					}
				break;
				
				case BLACK:
					if (bottomStatus % 2 == 1)
					{
						bottomStatus++;
					}
				break;
				
				case VOID:
					if (bottomStatus<2)
					{
						bottomStatus = -1;
						diff = diffMax;
					}
				break;
			}
		
			//top:				
			color = getColor(x, y - diff);
			
			switch (color)
			{
				case WHITE:
					if (topStatus % 2 == 0)
					{
						topStatus++;
					}
				break;
				
				case BLACK:
					if (topStatus % 2 == 1)
					{
						topStatus++;
					}
				break;
				
				case VOID:
					if (topStatus<2)
					{
						topStatus = -1;
						diff = diffMax;
					}
				break;
			}
		}
		
		if (leftStatus != 2 || rightStatus != 2  || bottomStatus != 2 || topStatus != 2)
		{
			return false;
		}
		
		return true;
	}
	
	private static int getColor(int x, int y)
	{
		if (x < 0 || y < 0 || x > pixeColors.length || y > pixeColors[0].length)
		{
			return VOID;
		}
		
		int r, g, b;
		
		r = (((int) pixeColors[x][y] & 0xff0000) >> 16); // red
		g = (((int) pixeColors[x][y] & 0xff00) >> 8); // green
		b = ((int) pixeColors[x][y] & 0xff); // blue	
		
		//white
		if (r > 128 && g > 128 && b > 128)
		{
			return WHITE;
		}
		
		//black
		if (r < 128 && g < 128 && b < 128)
		{
			return BLACK;
		}
		
		return VOID;
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

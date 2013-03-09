package de.samueltufan.qrdecoder;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import org.imgscalr.Scalr;

public class LocateQrCode
{
	private static int[][]			pixeColors;

	private static final int		BLACK	= 0;
	private static final int		WHITE	= 1;
	private static final int		VOID	= 2;

	private static final boolean	DEBUG	= true;

	private static BufferedImage	debugImage;
	private static File				f;
	
	private static ArrayList<Point> QrBlobPoints;

	public static BufferedImage locate(BufferedImage image, String name)
	{
		Graphics g;

		if (DEBUG)
		{
			f = new File("src/data/" + name + "debug.png");

			debugImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
			g = debugImage.getGraphics();			
		}
		
		g.setColor(Color.RED);
		findQrBlobs(image, g);
		
		g.setColor(Color.WHITE);
		Rectangle r = checkQRBlocks(g);
		
		if (DEBUG)
		{
			g.dispose();

			try
			{
				ImageIO.write(debugImage, "PNG", f);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		
		if (r != null)
		{
			BufferedImageOp[] ops = new BufferedImageOp[0];
			
			if (r.p.x < 0)
				r.p.x = 0;
			
			if (r.p.y < 0)
				r.p.y = 0;
			
			if (r.p.x + r.width >= image.getWidth())
				r.width = image.getWidth() - r.p.x;
			
			if (r.p.y + r.height >= image.getHeight())
				r.height = image.getHeight() - r.p.y;
			
			for (int y = 0; y < image.getHeight(); y++)
			{
				for (int x = 0; x < image.getWidth(); x++)
				{
					if (getColor(x, y) == BLACK)
					{
						image.setRGB(x, y, 0x000000);
					}
					else
					{
						image.setRGB(x, y, 0xffffff);
					}
				}
			}
			
			image = Scalr.crop(image, r.p.x, r.p.y, r.width, r.height, ops);			
			
			return image;
		}
		
		return null;
	}
	
	public static Rectangle checkQRBlocks(Graphics g)
	{	
		Point pa, pb, pc;
		Vector v0, v1, v2;
		float overflow = 2f;
		
		v0 = new Vector(new Point(0, 0), new Point(1, 0));
		
		for (int a=0; a<QrBlobPoints.size(); a++)
		{
			for (int b=0; b<QrBlobPoints.size(); b++)
			{
				for (int c=0; c<QrBlobPoints.size(); c++)
				{
					pa = QrBlobPoints.get(a);
					pb = QrBlobPoints.get(b);
					pc = QrBlobPoints.get(c);					
					
					if (Point.calculateLength(pa, pb) > 20 && Point.calculateLength(pa, pc) > 20)
					{	
						v1 = new Vector(pa, pb);
						v2 = new Vector(pa, pc);
						
						float angle = Vector.calculateAngle(v1, v2);
						
						if (angle > 85f && angle < 95f)
						{		
							float angle0 = Vector.calculateAngle(v0, v1);
							
							if (angle0 <= 5f)
							{
								if (Math.abs(v1.getAbsoluteValue() - v2.getAbsoluteValue()) <= v2.getAbsoluteValue() / 2)
								{									
									if (colorCountCenter(pa.x, pa.y, pb.x, pc.y, 0.25f))
									{									
										if (DEBUG)
										{
											g.drawLine(pa.x, pa.y, pb.x, pb.y);
											g.drawLine(pa.x, pa.y, pc.x, pc.y);
										}
										
										Point p = new Point(pa.x - (int) (pa.width * overflow), pa.y - (int) (pa.height * overflow));
										
										return new Rectangle(p, pb.x + (int) (pb.width * overflow) - p.x, pc.y + (int) (pc.height * overflow) - p.y);
									}
								}
							}
						}
					}
				}				
			}
		}
		
		return null;
	}

	public static void findQrBlobs(BufferedImage image, Graphics g)
	{
		int color, count = 0, countWhiteXTop = 0, countBlackXTop = 0, countWhiteYRight = 0, countBlackYRight = 0, countDiffXTop = 0, countDiffYRight = 0,
				countWhiteXBottom, countBlackXBottom, countDiffXBottom, countWhiteYLeft, countBlackYLeft, countDiffYLeft, fuziness = 15;

		QrBlobPoints = new ArrayList<Point>();
		
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
					if (count > 15 && count < 200)
					{
						countWhiteXTop = count;
						
						fuziness = (int) (countWhiteXTop * 0.25f);

						// check for a corresponding black line above the white
						// one
						countBlackXTop = colorCountX(x - countWhiteXTop, y - fuziness, x, y, BLACK);
						countDiffXTop = Math.abs(countBlackXTop - countWhiteXTop);

						if (countDiffXTop < diffCalc(countWhiteXTop, countBlackXTop, 0.25f))
						{
							// check for a corresponding white and black line
							// vertically below from this point
							countWhiteYRight = colorCountY(x - fuziness, y, x, y + countWhiteXTop, WHITE);
							countBlackYRight = colorCountY(x, y, x + fuziness, y + countWhiteXTop, BLACK);

							countDiffYRight = Math.abs(countBlackYRight - countWhiteYRight);

							if (countDiffYRight < diffCalc(countWhiteYRight, countBlackYRight, 0.25f))
							{
								//if (Math.abs(countWhiteXTop - countWhiteYRight) < 5)
								//{
									countWhiteXBottom = colorCountX(x - countWhiteXTop, y + countWhiteYRight - fuziness, x, y + countWhiteYRight, WHITE);
									countBlackXBottom = colorCountX(x - countWhiteXTop, y + countWhiteYRight, x, y + countWhiteYRight + fuziness, BLACK);
									
									countDiffXBottom = Math.abs(countWhiteXBottom - countBlackXBottom);
									
									if (countDiffXBottom < diffCalc(countWhiteXBottom, countBlackXBottom, 0.25f))
									{
										countWhiteYLeft = colorCountY(x - countWhiteXTop, y, x - countWhiteXTop + fuziness, y + countWhiteYRight, WHITE);
										countBlackYLeft = colorCountY(x - countWhiteXTop - fuziness, y, x - countWhiteXTop, y + countWhiteYRight, BLACK);
										
										countDiffYLeft = Math.abs(countWhiteYLeft - countBlackYLeft);
										
										if (countDiffYLeft < diffCalc(countWhiteYLeft, countBlackYLeft, 0.25f))
										{
											if(colorCountCenter(x - countWhiteXTop, y, x, y + countWhiteYRight, 0.2f))
											{													
												QrBlobPoints.add(new Point(x - countWhiteXTop/2, y + countWhiteYRight/2, countWhiteXTop, countWhiteYRight));
												
												//System.out.println("QR Blob on: P00(" + (x - countWhiteXTop) + ", " + y + ") P10(" + x + ", " + y + ") P01(" + (x - countWhiteXTop) + ", " + (y + countWhiteYRight) + ") P11(" + x + ", " + (y + countWhiteYRight) + ")");
											
												if (DEBUG)
												{
													g.drawLine(x - countWhiteXTop, y, x, y);
													g.drawLine(x, y, x, y + countWhiteYRight);
													g.drawLine(x - countWhiteXTop, y, x - countWhiteXTop, y + countWhiteYRight);
													g.drawLine(x - countWhiteXTop, y + countWhiteYRight, x, y + countWhiteYRight);
												}
											}
										}
									}
								//}
							}
						}
					}

					count = 0;
				}
			}
		}
	}
	
	public static int diffCalc(int a, int b, float percent)
	{
		int max = Math.max(a, b);
		
		return (int) (max * percent);
	}
	
	public static int colorCountX(int xstart, int ystart, int xend, int yend, int color)
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

	public static int colorCountY(int xstart, int ystart, int xend, int yend, int color)
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
	
	public static boolean colorCountCenter(int xstart, int ystart, int xend, int yend, float percent)
	{
		int count = 0;

		for (int x = xstart; x <= xend; x++)
		{
			for (int y = ystart; y <= yend; y++)
			{
				if (getColor(x, y) == BLACK)
				{
					count++;
				}
			}
		}

		float area = (xend - xstart) * (yend - ystart);
		
		if (area * percent <= count)
		{
			return true;
		}
		
		return false;
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

		if (getBrightness(r, g, b) >= 0.5)
		{
			return WHITE;
		}

		if (getBrightness(r, g, b) < 0.5)
		{
			return BLACK;
		}

		return VOID;
	}

	private static float getBrightness(int r, int g, int b)
	{
		float brightness;

		int cmax = (r > g) ? r : g;
		if (b > cmax)
			cmax = b;

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
			for (int pixel = 0, row = 0, col = 0; pixel < pixels.length; pixel += pixelLength)
			{
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
				// argb += -16777216; // 255 alpha
				argb += ((int) pixels[pixel] & 0xff); // blue
				argb += (((int) pixels[pixel + 1] & 0xff) << 8); // green
				argb += (((int) pixels[pixel + 2] & 0xff) << 16); // red
				// result[row][col] = argb;
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

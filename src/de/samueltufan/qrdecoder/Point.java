package de.samueltufan.qrdecoder;

public class Point
{
	public int x, y, width, height;
	
	public Point(int x, int y)
	{
		this(x, y, 0, 0);
	}
	
	public Point(int x, int y, int width, int height)
	{
		this.x = x;
		this.y = y;
		
		this.width = width;
		this.height = height;
	}
	
	public static float calculateLength(Point p1, Point p2)
	{
		int a = p1.x - p2.x;
		int b = p1.y - p2.y;
		
		return (float) Math.sqrt(a*a + b*b);
	}
}

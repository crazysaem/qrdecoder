package de.samueltufan.qrdecoder;

public class Vector
{
	private int x, y;
	
	public Vector (Point p1, Point p2)
	{
		this.x = p2.x - p1.x;
		this.y = p2.y - p1.y;
	}
	
	public static float calculateAngle(Vector v1, Vector v2)
	{
		float top = v1.x * v2.x + v1.y * v2.y;
		float bot = v1.getAbsoluteValue() * v2.getAbsoluteValue();
		
		return (float) ((float) Math.acos(top / bot) * 180 / Math.PI);
	}
	
	public float getAbsoluteValue()
	{
		return (float) Math.sqrt(this.x*this.x + this.y*this.y);
	}
}

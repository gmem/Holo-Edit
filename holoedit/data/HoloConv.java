package holoedit.data;

public class HoloConv {
	
	public static float rad2Deg(float rad)
	{
		return ((float)(-rad * 180.f / Math.PI) + 450.f) % 360.f;
	}
	
	public static float deg2Rad(float deg)
	{
		return -(deg - 90.f) * (float)Math.PI / 180.f;
	}
	
	public static double deg2Rad(double deg)
	{
		return -(deg - 90.f) * (float)Math.PI / 180.f;
	}
	
}

package holoedit.data;

public class HoloVec3 {
	
	public double x = 0.;
	public double y = 0.;
	public double z = 0.;
	
	public HoloVec3()
	{
		
	}
	
	public HoloVec3(double x,double y,double z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public HoloVec3 add(double x,double y,double z)
	{
		
		return new HoloVec3(this.x+x,this.y+y,this.z+z);
	}
	
	public HoloVec3 add(HoloVec3 p)
	{
		
		return new HoloVec3(this.x+p.x,this.y+p.y,this.z+p.z);
	}
	
	
	public HoloVec3 mult(double x,double y,double z)
	{
		
		return new HoloVec3(this.x*x,this.y*y,this.z*z);
	}
	
	public HoloVec3 mult(HoloVec3 p)
	{
		
		return new HoloVec3(this.x*p.x,this.y*p.y,this.z*p.z);
	}
	
	public void addin(double x,double y,double z)
	{
		this.x += x;
		this.y += y;
		this.z += z;
	}
	
	public void addin(HoloVec3 p)
	{
		this.x += p.x;
		this.y += p.y;
		this.z += p.z;
	}
	
	public void multin(double x,double y,double z)
	{
		this.x *= x;
		this.y *= y;
		this.z *= z;
	}
	
	public void multin(HoloVec3 p)
	{
		this.x *= p.x;
		this.y *= p.y;
		this.z *= p.z;
	}

}

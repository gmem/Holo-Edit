package holoedit;

import com.cycling74.max.*;

public class hostname extends MaxObject {
	
	hostname()
	{
		declareInlets(new int[] {DataTypes.ALL});
		declareOutlets(new int[] {DataTypes.ALL});
	}
	
	public void bang()
	{
		try
		{
			java.net.InetAddress localMachine = java.net.InetAddress.getLocalHost();	
			outlet(0, localMachine.getHostName());
		}
		catch(java.net.UnknownHostException uhe)
		{
			error("holoedit.hostname : can't get hostname");
		}
	}

}

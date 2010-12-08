/**
 *  -----------------------------------------------------------------------------
 *  
 *  Holo-Edit, spatial sound trajectories editor, part of Holophon
 *  Copyright (C) 2006 GMEM
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA 
 *  
 *  -----------------------------------------------------------------------------
 */
package holoedit.rt;

import holoedit.HoloEdit;
import holoedit.util.Ut;
import java.net.BindException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.EventObject;

import com.illposed.osc.OSCPortIn;
import com.illposed.osc.OSCPortOut;

public class OSCConnection extends Connection
{
	public OSCConnection(HoloEdit m)
	{
		super(m);
	}
		
	public void open()
	{
		System.out.println("osc open");
		if(!open)
		{
			open = true;
			newReceiver();
			newSender();
		}
	}

	protected void newSender()
	{
		System.out.println("NEWSENDER OSC");
		try
		{
			sender = new OSCPortOut(InetAddress.getByName(address),out);
		}
		catch (BindException be)
		{
		}
		catch (Exception e)
		{
			open = false;
			e.printStackTrace();
		}
		if(sender != null)
		{
			send(keyOut+"/connection/holoedit/name", new Object[]{currentEditName});
				try	{					
					InetAddress a = InetAddress.getByName(address);
					send(keyOut+"/connection/holoedit/address",new Object[]{a.getHostAddress(),in});
				}
				catch (UnknownHostException e)
				{
					System.err.println("OSCConnection : UnknownHostException while trying to send his own address.");
					e.printStackTrace();
				}
			send(keyOut+"/connection/holoedit/connect",new Object[]{1});
			send(keyOut+"/connection/holoedit/key",new Object[]{keyIn});
			send(keyOut+"/connection/holospat/querykey",new Object[]{"bang"});
		}
	}

	protected void newReceiver()
	{
		if(receiver != null)
		{
			receiver.stopListening();
			receiver.close();
		}
		try
		{
			try
			{
				receiver = new OSCPortIn(in);
			}
			catch (BindException be)
			{
				int oldIn = in;
				in++;
				Ut.alert("Port already in use","Port n¡"+oldIn+" is already in use, setting it to : "+in);
				receiver = new OSCPortIn(in);
			}		
		} catch (SocketException e) {
			Ut.alert("Error : SocketException","Problem while creating an input connection,\nTransport won't be available.\nplease see the console\n(Application/Utilities/Console.app)\nfor more informations.");
			e.printStackTrace();
			open = false;
			receiver = null;
			Ut.barMenu.update();
		}

		if(receiver != null)
		{
			receiver.addListener(keyIn,this);
			receiver.startListening();
		}
	}
	
	public void close()
	{
		System.out.println("osc close");
		holoEditRef.transport.setConnectionStatus(" -- ", false);
		if(open)
		{
			if(playing || paused)
				stop();
			if(sender != null)
			{
				send(keyOut+"/connection/holoedit/connect",new Object[]{0});
			}
			if(receiver != null)
				receiver.stopListening();
			open = false;
		}
	}	
}


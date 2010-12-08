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
import java.util.HashMap;
import java.util.Vector;
import javax.swing.JOptionPane;
import com.apple.dnssd.BrowseListener;
import com.apple.dnssd.DNSSD;
import com.apple.dnssd.DNSSDException;
import com.apple.dnssd.DNSSDRegistration;
import com.apple.dnssd.DNSSDService;
import com.apple.dnssd.RegisterListener;
import com.apple.dnssd.ResolveListener;
import com.apple.dnssd.TXTRecord;
import com.illposed.osc.OSCPortIn;
import com.illposed.osc.OSCPortOut;

public class BonjourConnection extends Connection implements RegisterListener, BrowseListener, ResolveListener
{
	// OSC bonjour REGTYPE
	public String registeringService = "_osc._udp";
	private DNSSDRegistration bonjourConnection;
	public HashMap<String,String> spatmap = new HashMap<String,String>();
	private boolean browsing = false;

	public BonjourConnection(HoloEdit m) {
		super(m);
	}
		
	public void open() {
		if(!open) {
			open = true;
			newReceiver();
			if(holoEditRef.bonjour)
				register();
			else
				newSender();
		}
	}

	/**   Register a service, to be discovered via browse() and resolve() calls. */
	private void register()
	{
		try
		{
			holoEditRef.transport.setConnectionStatus("@"+address+":"+out, true);
			DNSSD.register(0, 0, currentEditName, registeringService, null, null, in, null, this);
			if(!browsing)
			{
				DNSSD.browse(0,0,registeringService, null, this);
				browsing = true;
			}
			
		} catch (DNSSDException e) {
			System.err.println("OSCConnection : DNSSDException while trying to register.");
			e.printStackTrace();
			close();
		}
	}

	/** Resolve a service name discovered via browse() to a target host name, port number, and txt record.*/
	private void resolve()
	{
		try
		{
			if(currentSpatName != null)
				DNSSD.resolve(0,0,currentSpatName,registeringService,currentSpatDomain,this);
		} catch (DNSSDException e) {
			System.err.println("OSCConnection : DNSSDException while trying to resolve name : "+currentSpatName);
			e.printStackTrace();
			close();
		}
	}

	protected void newSender()
	{
		try
		{
			sender = new OSCPortOut(InetAddress.getByName(address),out);
		} catch (BindException be)
		{
			System.err.println(be.getMessage());
		}
		catch (Exception e)
		{
			open = false;
			System.err.println(e.getMessage());
			//e.printStackTrace();
		}

		if(sender != null && open)
		{
			if(holoEditRef.bonjour){
				send(keyOut+"/connection/holoedit/name", new Object[]{currentEditName});
			}else {
				try
				{
					InetAddress a = InetAddress.getByName(address);
					send(keyOut+"/connection/holoedit/address",new Object[]{a.getHostAddress(),in});
				}
				catch (UnknownHostException e)
				{
					holoEditRef.transport.setConnectionStatus(" -- ", false);
					System.err.println("OSCConnection : UnknownHostException while trying to send his own address.");
					e.printStackTrace();
				}
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
		try	{
			try	{
				receiver = new OSCPortIn(in);
			} catch (BindException be) {
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
		if(open)
		{
			if(playing || paused)
				stop();
			if(bonjourConnection != null)
			{
				bonjourConnection.stop();
				currentSpatDomain = "";
				currentSpatName = "";
			}
			if(sender != null)
			{
				send(keyOut+"/connection/holoedit/connect",new Object[]{0});
			}
			//if(receiver != null)
			//	receiver.stopListening();
			open = false;
			holoEditRef.transport.setConnectionStatus(" -- ", false);
		}
	}
	
	public void serviceRegistered(DNSSDRegistration registration, int flags, String serviceName, String regType, String domain)
	{
		System.out.println("Registered on bonjour with serviceName >"+serviceName+"< and domain >"+domain+"<");
		bonjourConnection = registration;
		currentEditName = serviceName;
	}

	public void operationFailed(DNSSDService service, int errorCode)
	{
		System.out.println("Registration Failed : "+service+" "+errorCode);
		service.stop();
		bonjourConnection = null;
	}

	public void serviceFound(DNSSDService browser, int flags, int ifIndex, String serviceName, String regType, String domain)
	{
		if(holoEditRef.bonjour && !serviceName.equalsIgnoreCase(this.currentEditName))
		{
			holoEditRef.connectionPanel.addConnectChoiceItem(serviceName); // ajout a la liste de connectionPanel
			this.currentSpatDomain = domain;
			spatmap.put(serviceName,domain);
			if(flags != DNSSD.MORE_COMING && !spatmap.isEmpty())
			{
				String newSpatName = "";
				String newSpatDomain = "";
				Vector<String> srvs = new Vector<String>(spatmap.keySet());
				Vector<String> doms = new Vector<String>(spatmap.values());
				if(spatmap.size() == 1)
				{
					newSpatName = srvs.get(0);
					newSpatDomain = doms.get(0);
				} else {
		/*			String result = (String)JOptionPane.showInputDialog(null,"Multiple Bonjour Services Found on the network","Choose Holo-Spat",JOptionPane.QUESTION_MESSAGE,null,srvs.toArray(),srvs.get(0));
					if(result != null)
					{
						newSpatName = result;
						newSpatDomain = spatmap.get(currentSpatName);
					}*/
				}
				if(!currentSpatName.equalsIgnoreCase(newSpatName) && !newSpatName.equalsIgnoreCase(""))
				{
					currentSpatName = newSpatName;
					currentSpatDomain = newSpatDomain;
					resolve();;
				}
			}
		}
	}

	public void serviceLost(DNSSDService browser, int flags, int ifIndex, String serviceName, String regType, String domain)
	{
		System.out.println("Lost service : "+serviceName);
		holoEditRef.connectionPanel.removeConnectChoiceItem(serviceName);
		if(holoEditRef.bonjour && !serviceName.equalsIgnoreCase(this.currentEditName))
		{
			spatmap.remove(serviceName);
			if(currentSpatName.equalsIgnoreCase(serviceName))
			{
				holoEditRef.transport.setConnectionStatus(" -- ", false); // on perd la connection
				JOptionPane.showMessageDialog(null,"Holo-Spat ( "+serviceName+" ) is disconnected","Holo-Spat lost",JOptionPane.WARNING_MESSAGE);
				close();
				Ut.barMenu.update();
			}
		}
	}

	public void serviceResolved(DNSSDService resolver, int flags, int ifIndex, String fullName, String hostName, int port, TXTRecord txtRecord)
	{
		System.out.println("Connecting to spat > service name : "+currentSpatName+" domain : "+currentSpatDomain+" IP : "+hostName+" port : "+port);
		holoEditRef.transport.setConnectionStatus(currentSpatName, true);
		if(flags != DNSSD.MORE_COMING) //  DNSSD.MORE_COMING Flag indicates to a BrowseListener that another result is queued.
		{
			resolver.stop();
			address = hostName;
			out = port;
			newSender();
			
		}
	}
}
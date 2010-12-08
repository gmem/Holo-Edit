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
package holoedit.util;

import holoedit.HoloEdit;
import com.apple.eawt.Application;
import com.apple.eawt.ApplicationEvent;

public class MacApplication
{
	Application app;
	HoloEdit holoEditRef;
	
	public MacApplication(HoloEdit owner)
	{
		app = Application.getApplication();
		holoEditRef = owner;
		app.setEnabledPreferencesMenu(true);
		app.addApplicationListener(new com.apple.eawt.ApplicationListener()
		{
			public void handleAbout(ApplicationEvent e)
			{
				holoEditRef.about();
				e.setHandled(true);
			}

			public void handleOpenApplication(ApplicationEvent e)
			{
				e.setHandled(true);
			}

			public void handleOpenFile(ApplicationEvent e)
			{
				try
				{
					holoEditRef.openFileName = e.getFilename();
					if(holoEditRef.readyToOpen)
						holoEditRef.gestionPistes.readDroppedFile(holoEditRef.openFileName);
				} catch (Exception ex) {}
				e.setHandled(true);
			}

			public void handlePreferences(ApplicationEvent e)
			{
				holoEditRef.openPrefs();
				e.setHandled(true);
			}

			public void handlePrintFile(ApplicationEvent e)
			{}

			public void handleQuit(ApplicationEvent e)
			{
				e.setHandled(holoEditRef.close());
			}

			public void handleReOpenApplication(ApplicationEvent e)
			{
				e.setHandled(true);
			}
		});
	}
}

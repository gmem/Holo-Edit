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
package holoedit.gui;

import java.awt.Component;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class TrackPopup extends PopupMenu implements ActionListener
{
	public GestionPistes gpRef;
	private int num;
	private MenuItem init = new MenuItem("Init");
	private MenuItem name = new MenuItem("Set Name...");
	private MenuItem number = new MenuItem("Set Number...");
	private MenuItem color = new MenuItem("Set Color...");
	private MenuItem lock = new MenuItem("Lock/Unlock");
	private MenuItem add = new MenuItem("New Track");
	private MenuItem delete = new MenuItem("Delete");
	private MenuItem duplicate = new MenuItem("Duplicate");
	private MenuItem resetnames = new MenuItem("Reset all names");
	private MenuItem resetnumbers = new MenuItem("Reset all numbers");
	private MenuItem renumber = new MenuItem("Renumber all tracks...");
	private MenuItem reset = new MenuItem("Reset both");
	private MenuItem importT = new MenuItem("Import Track...");
	private MenuItem exportT = new MenuItem("Export Track...");

	public TrackPopup(GestionPistes gp, int tkNum)
	{
		gpRef = gp;
		num = tkNum;
		add(init);
		add(name);
		add(number);
		add(color);
		addSeparator();
		add(lock);
		addSeparator();
		add(add);
		add(delete);
		add(duplicate);
		addSeparator();
		add(resetnumbers);
		add(resetnames);
		add(renumber);
		add(reset);
		addSeparator();
		add(importT);
		add(exportT);
		init.addActionListener(this);
		name.addActionListener(this);
		number.addActionListener(this);
		color.addActionListener(this);
		lock.addActionListener(this);
		add.addActionListener(this);
		delete.addActionListener(this);
		duplicate.addActionListener(this);
		resetnames.addActionListener(this);
		resetnumbers.addActionListener(this);
		renumber.addActionListener(this);
		reset.addActionListener(this);
		importT.addActionListener(this);
		exportT.addActionListener(this);
	}

	public void setNumber(int n)
	{
		num = n;
	}
	
	public void show(Component origin, int x, int y)
	{
		lock.setLabel(gpRef.getTrack(num).isLocked() ? "Unlock" : "Lock");
		super.show(origin,x,y);
	}
	
	public void actionPerformed(ActionEvent e)
	{
		Object o = e.getSource();
		if (o == init)
			gpRef.initOneTrack(num);
		else if (o == name)
			gpRef.changeName(num);
		else if (o == number)
			gpRef.changeNumber(num);
		else if (o == color)
			gpRef.changeColor(num);
		else if (o == lock)
			gpRef.changeLock(num);
		else if (o == add)
			gpRef.addTrack();
		else if (o == delete)
			gpRef.deleteTrack(num);
		else if (o == duplicate)
			gpRef.duplicateTrack(num);
		else if (o == resetnames)
			gpRef.resetnames();
		else if (o == resetnumbers)
			gpRef.resetnumbers();
		else if (o == renumber)
			gpRef.changeNumbers();
		else if (o == reset)
			gpRef.reset();
		else if (o == exportT)
			gpRef.exportTrack(gpRef.getActiveTrackNb());
		else if (o == importT)
			gpRef.importTrack(num,gpRef.getTrack(num).getLastDate());
		gpRef.holoEditRef.score.popupVisible = false;
		gpRef.holoEditRef.room.display();
	}
}

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
package holoedit.functions;

import holoedit.data.HoloTrack;
import holoedit.data.HoloTraj;
import holoedit.data.WaveFormInstance;
import holoedit.gui.GestionPistes;
import holoedit.gui.ProgressBar;
import holoedit.util.Ut;

public class Algors
{
	// date de la sélection temporelle pour les transformations et les algorithmes génératifs en mode 'replace'
	public int dateBegin, dateEnd;
	// barre de progression pour suivre l'evolution des calculs
	ProgressBar bP;
	// le thread qui va executer l'algorithme dans un processus different
	Thread runner;
	// references sur gestionPiste (pour avoir acces aux pistes, au zoom...)
	GestionPistes gp;
	private HoloTrack tmpTrack, tmpTrackA, tmpTrackB, newPiste;
	
	// constructeurs
	public Algors()
	{
	}

	public Algors(GestionPistes _gp)
	{
		gp = _gp;
	}

	// ------------------------- COPY -------------------------
	public HoloTrack Copy(HoloTrack track, boolean copyWaves)
	{
		newPiste = track.dupliquer();
		newPiste.crop(dateBegin, dateEnd, copyWaves);
		newPiste.timeShift(-dateBegin);
		newPiste.update();
		return newPiste;
	}

	// ------------------------- PASTE -------------------------
	public void Paste(HoloTrack track, HoloTrack trackCopy, boolean pasteWaves)
	{
		if (trackCopy == null || trackCopy.isEmpty())
			return;
		if (track.isEmpty())
		{
			for (HoloTraj ht : trackCopy.trajs)
				track.addTraj(ht.dupliquer(), dateBegin + ht.getFirstDate());
			if (pasteWaves)
				for (WaveFormInstance w : trackCopy.waves)
				{
					w = w.dupliquer();
					w.shiftDates(dateBegin);
					track.addWave(w);
				}
		}
		else
		{
			int date;
			if (track.waves.isEmpty() || !pasteWaves)
				date = track.getLastDate();
			else date = Ut.max(track.getLastDate(), track.waves.lastElement().getLastDate());
			if (date < dateBegin)
			{
				for (HoloTraj ht : trackCopy.trajs)
					track.addTraj(ht.dupliquer(), dateBegin + ht.getFirstDate());
				if (pasteWaves)
					for (WaveFormInstance w : trackCopy.waves)
					{
						w = w.dupliquer();
						w.shiftDates(dateBegin);
						track.addWave(w);
					}
			}
			else
			{
				for (HoloTraj ht : trackCopy.trajs)
					track.addTraj(ht.dupliquer(), date + ht.getFirstDate());
				if (pasteWaves)
					for (WaveFormInstance w : trackCopy.waves)
					{
						w = w.dupliquer();
						w.shiftDates(date);
						track.addWave(w);
					}
			}
		}
		track.update();
	}

	// ------------------------- REPLACE -------------------------
	public void Replace(HoloTrack track, HoloTrack trackCopy, boolean replaceWaves)
	{
		if (trackCopy == null || trackCopy.isEmpty())
			return;
		if (track.isEmpty() || (track.getLastDate() < dateBegin))
			Paste(track, trackCopy, replaceWaves);
		else
		{
			int date;
			if (trackCopy.waves.isEmpty() || !replaceWaves)
				date = trackCopy.getLastDate();
			else
				date = Ut.max(trackCopy.getLastDate(), trackCopy.waves.lastElement().getLastDate());
			track.cut(dateBegin - 1, date + dateBegin + 1, replaceWaves, false);
			for (HoloTraj ht : trackCopy.trajs)
				track.addTraj(ht.dupliquer(), dateBegin + ht.getFirstDate());
			if (replaceWaves)
				for (WaveFormInstance w : trackCopy.waves)
				{
					w = w.dupliquer();
					w.shiftDates(dateBegin);
					track.addWave(w);
				}
		}
		track.update();
	}

	// ------------------------- INSERT -------------------------
	public void Insert(HoloTrack track, HoloTrack trackCopy, boolean insertWaves)
	{
		if (trackCopy == null || trackCopy.isEmpty())
			return;
		if (track.isEmpty() || track.getLastDate() < dateBegin)
			Paste(track, trackCopy, insertWaves);
		else
		{
			int date;
			if (trackCopy.waves.isEmpty() || !insertWaves)
				date = trackCopy.getLastDate();
			else
				date = Ut.max(trackCopy.getLastDate(), trackCopy.waves.lastElement().getLastDate());
			int ind = track.cutAt(dateBegin);
			if (ind != -1)
			{
				track.trajs.get(ind).firstElement().date += 1;
				for (int k = ind, last = track.trajs.size(); k < last; k++)
					track.trajs.get(k).shiftDates(date);
			}
			if (insertWaves)
				for (WaveFormInstance w : track.waves)
					if (w.getFirstDate() >= dateBegin)
						w.shiftDates(date);
			for (HoloTraj ht : trackCopy.trajs)
				track.addTraj(ht.dupliquer(), dateBegin + ht.getFirstDate());
			if (insertWaves)
				for (WaveFormInstance w : trackCopy.waves)
				{
					w = w.dupliquer();
					w.shiftDates(dateBegin);
					track.addWave(w);
				}
		}
		track.update();
	}

	// ------------------------- EFFACER -------------------------
	public void Erase(HoloTrack track, boolean eraseWaves)
	{
		track.cut(dateBegin - 1, dateEnd + 1, eraseWaves, false);
		track.update();
	}
}

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
/**
 * Classe permettant l'affichage d'une barre de progression pour suivre un calcul en arriere plan et sa progression
 * Utilise dans les fonctions de lecture/ecriture, les algorithme de transformation/creation de trajectoires.
 * (! necessite d'effectuer le calcul dans un processus different)
 */

package holoedit.gui;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.swing.plaf.metal.MetalProgressBarUI;

public class ProgressBar extends FloatingWindow implements Runnable
{
	private Thread runner;
	
	/** la barre proprement dite */
	private JProgressBar barre;
	/** ancienne valeur */
	private int opVal = 0;
	/** valeur courante */
	private int pVal = 0;
	/** fonte */
	private Font f = new Font("geneva", Font.PLAIN, 10);

	//
	private int compteur = 0;

	// Constructeur avec titre
	public ProgressBar(String Title)
	{
		super(Title, null, 100, 20, 0, 0, true);
		setLayout(new FlowLayout());
		barre = new JProgressBar(0, 16);
		barre.setUI(new MetalProgressBarUI());
		barre.setForeground(Color.BLUE.darker().darker().darker());
		barre.setOpaque(false);
		barre.setValue(0);
		barre.setStringPainted(false);
		barre.setVisible(true);
		add(barre);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		compteur = 0;
		pack();
	}

	/**
	 * Sets the indeterminate property of the progress bar, which determines whether</b>
	 * the progress bar is in determinate or indeterminate mode. An indeterminate progress bar</b>
	 * continuously displays animation indicating that an operation of unknown length is occurring.
	 */
	public void setIndeterminate(Boolean indeterminate) {
		barre.setIndeterminate(indeterminate);
	}
	
	/** Mise a jour de la valeur. */
	public void setValue(int value) {
		compteur = value;
		barre.setValue(value);
	}

	public void repaint()
	{
		pVal = (int) (barre.getPercentComplete() * 100);
		if (pVal != opVal)
		{
			opVal = pVal;
			repaint(10);
		}
	}

	/** Affectation du maximum. */
	public void setMaximum(int max)
	{
		barre.setMaximum(max);
	}
	
	public void inc()
	{
		compteur++;
		barre.setValue(compteur);
	}
	
	public void dec()
	{
		compteur--;
		barre.setValue(compteur);
	}
	
	public void inc(int i)
	{
		compteur+=i;
		barre.setValue(compteur);
	}
	
	public void open()
	{
		super.open();
		runner = new Thread(this);
		runner.setName("progress_bar");
		runner.setPriority(Thread.MIN_PRIORITY);
		runner.setDaemon(true);
		runner.start();
	}
	
	public void run()
	{
		try
		{
			while(visible)
			{
				repaint();
				Thread.sleep(50);
			}
		}
		catch (InterruptedException e)
		{
			super.close();
		}
	}
	
	public void close()
	{
		if(runner != null)
			runner.interrupt();
	}
	
	public void windowClosing(WindowEvent e)
	{
		close();
	}
	
	public void dispose()
	{
		close();
	}
}
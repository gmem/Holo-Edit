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

/** La classe Compteur qui fabrique un compteur hh:mm:ss:centieme de s* */
public class CounterCalculation
{
	private int heure;
	private int minute;
	private int seconde;
	private int milliSeconde;

	// cons. par defaut
	public CounterCalculation()
	{
		heure = 0;
		minute = 0;
		seconde = 0;
		milliSeconde = 0;
	}

	// avec initialisation
	public CounterCalculation(int h, int m, int s, int ms)
	{
		heure = h;
		minute = m;
		seconde = s;
		milliSeconde = ms;
	}

	/** methode pour modifier la date */
	public void setValeur(int H, int M, int S, int Ms)
	{
		while (Ms >= 1000)
		{
			Ms = Ms - 1000;
			S = S + 1;
		}
		while (S >= 60)
		{
			S = S - 60;
			M = M + 1;
		}
		while (M >= 60)
		{
			M = M - 60;
			H = H + 1;
		}
		heure = H;
		minute = M;
		seconde = S;
		milliSeconde = Ms;
	}

	/** methode pour modifier la date */
	public void setValeurDelta(int S, int Ms)
	{
		while (Ms >= 1000)
		{
			Ms = Ms - 1000;
			S = S + 1;
		}
		heure = 0;
		minute = 0;
		seconde = S;
		milliSeconde = Ms;
	}

	/** methode de remise a zero du compteur */
	public void raz()
	{
		heure = 0;
		minute = 0;
		seconde = 0;
		milliSeconde = 0;
	}

	/** methodes d'incrementation */
	public void addHeures(double absTime, int val)
	{
		double absNewTime = absTime + val * 3600000;
		if (absNewTime < 0)
			absNewTime = 0;
		toCompteur(absNewTime);
	}

	public void addMinutes(double absTime, int val)
	{
		double absNewTime = absTime + val * 60000;
		if (absNewTime < 0)
			absNewTime = 0;
		toCompteur(absNewTime);
	}

	public void addSecondes(double absTime, int val)
	{
		double absNewTime = absTime + val * 1000;
		if (absNewTime < 0)
			absNewTime = 0;
		toCompteur(absNewTime);
	}

	public void addSecondesD(double absTime, int val)
	{
		double absNewTime = absTime + val * 1000;
		if (absNewTime < 100)
			absNewTime = 100; // valeur min pour delta : 10 centisec
		if (absNewTime > 60000)
			absNewTime = 60000;
		toCompteur(absNewTime);
	}

	public void addMilliSecondes(int absTime, int val)
	{
		int absNewTime = absTime + val;
		if (absNewTime < 0)
			absNewTime = 0;
		toCompteur(absNewTime);
	}

	/** pour recuperer la valeur du compteur */
	public int getValHeure()
	{
		return heure;
	}

	public int getValMinute()
	{
		return minute;
	}

	public int getValSeconde()
	{
		return seconde;
	}

	public int getValCentiSeconde()
	{
		return milliSeconde/10;
	}
	
	public int getValMilliSeconde()
	{
		return milliSeconde;
	}

	/** passage du temps compteur en milliseconde */
	public int toCentiSeconde()
	{
		return ((milliSeconde/10) + 100 * (seconde + 60 * (minute + 60 * (heure))));
	}

	public int toMilliSeconde()
	{
		return (milliSeconde + 1000 * (seconde + 60 * (minute + 60 * (heure))));
	}

	/** passage du temps en milliseconde en temps compteur */
	public boolean toCompteur(double temps)
	{
		if(temps != toMilliSeconde())
		{
			raz();
			// if (temps < 0) temps = 0 ;
			heure = (int) (temps / 3600000);
			minute = (int) ((temps - (heure * 3600000)) / 60000);
			seconde = (int) ((temps - (heure * 3600000) - (minute * 60000)) / 1000);
			milliSeconde = (int) (temps - (heure * 3600000) - (minute * 60000) - (seconde * 1000));
			return true;
		}
		return false;
	}

	/** soustraction de temps, retourne un temp en milliseconde */
	public CounterCalculation soustract(CounterCalculation c)
	{
		CounterCalculation res = new CounterCalculation();
		double temps1 = this.toMilliSeconde();
		double temps2 = c.toMilliSeconde();
		res.toCompteur(temps2 - temps1);
		return res;
	}

	/** addition de temps, retourne un temp en milliseconde */
	public CounterCalculation addition(CounterCalculation c)
	{
		CounterCalculation res = new CounterCalculation();
		double temps1 = this.toMilliSeconde();
		double temps2 = c.toMilliSeconde();
		res.toCompteur(temps2 + temps1);
		return res;
	}

	/** comparaison de deux compteur*/
	public boolean inferieur(CounterCalculation c)
	{
		if (this.soustract(c).toMilliSeconde() < 0)
			return false;
		return true;
	}
}
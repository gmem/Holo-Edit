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

public class Formatter
{
	public int min_right = -1;
	public int max_right = -1;
	public int min_left = -1;
	public int max_left = -1;
	
	public Formatter()
	{
	}
	
	public Formatter(int minl, int maxl, int minr, int maxr)
	{
		min_left = minl;
		max_left = maxl;
		min_right = minr;
		max_right = maxr;
	}
	
	public String format(double f)
	{
		String tmp = "" + f;
		int EXP = tmp.indexOf('E');
		if(EXP != -1)
		{
			String a = tmp.substring(0,tmp.indexOf('.'));
			boolean neg = a.indexOf('-') != -1;
			a = neg ? a.substring(1) : a;
			String b = tmp.substring(tmp.indexOf('.')+1,tmp.indexOf('.')+2);
			int E = Integer.parseInt(tmp.substring(EXP+1));
			String tmp2 = "0.";
			while(E < -a.length())
			{
				tmp2 += '0';
				E++;
			}
			tmp2 += a + (b.equalsIgnoreCase("0") ? "" : b);
			
			if(neg) tmp2 = '-'+tmp2;
			
			tmp = tmp2;
		}
		int ind = tmp.indexOf('.');
		if(ind != -1)
		{
			String left = tmp.substring(0,ind);
			String right = tmp.substring(ind+1);
			if(max_right != -1 && right.length() > max_right)
			{
				right = right.substring(0,max_right);
			}
			if(min_right != -1 && right.length() < min_right)
			{
				while (right.length() < min_right)
					right = right + '0';
			}	
			if(max_left != -1 && left.length() > max_left)
			{
				left = left.substring(left.length() - max_left);
			}
			if(min_left != -1 && left.length() < min_left)
			{
				while (left.length() < min_left)
					left = '0' + left;
			}	
			return left + '.' + right;
		}
		if(max_left != -1 && tmp.length() > max_left)
		{
			tmp = tmp.substring(tmp.length() - max_left);
		}
		if(min_left != -1 && tmp.length() < min_left)
		{
			while (tmp.length() < min_left)
				tmp = '0' + tmp;
		}	
		return tmp;
	}
}

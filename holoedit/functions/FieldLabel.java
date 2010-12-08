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
/** ************* classe FieldLabel ****************** */
/* Nom d'un champ d'entree des fonctions */
package holoedit.functions;

import java.awt.*;
import javax.swing.*;

class FieldLabel extends JLabel
{
	// attributs graphiques
	private int w = 180;
	private int h = 25;
	private Color forgColor = Color.black;
	private Font fo = new Font("Verdana", 0, 12);

	public FieldLabel(String title)
	{
		setText(title + " :");
		setForeground(forgColor);
		setVisible(true);
		setHorizontalAlignment(javax.swing.JTextField.CENTER);
		setSize(new Dimension(w, h));
		setFont(fo);
	}
}

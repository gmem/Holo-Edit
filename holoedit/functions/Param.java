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
/** ************* classe Param ****************** */
/* Champ d'entree generique champ d'une fenetre de fonction */
package holoedit.functions;

import java.awt.Font;
import java.awt.Point;
import java.awt.event.FocusListener;
import java.awt.event.ItemListener;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;

public interface Param
{
	// fonte
	Font f = new Font("courrier", Font.PLAIN, 12);
	Font f2 = new Font("courrier", Font.PLAIN, 10);

	static int TYPE_INT = 0;
	static int TYPE_FLOAT = 1;
	static int TYPE_DOUBLE = 2;
	static int TYPE_TEXT = 3;
	static int TYPE_CHECK = 4;
	static int TYPE_COMBO = 5;
	static int TYPE_BUTTON = 6;
	static Class<Integer> RETURN_TYPE_INT = Integer.class;
	static Class<Float> RETURN_TYPE_FLOAT = Float.class;
	static Class<Double> RETURN_TYPE_DOUBLE = Double.class;
	static Class<String> RETURN_TYPE_TEXT = String.class;
	static Class<Boolean> RETURN_TYPE_CHECK = Boolean.class;
	static Class<Integer> RETURN_TYPE_COMBO = Integer.class;
	static Class<String> RETURN_TYPE_BUTTON = String.class;
	
	// recupere le numero du champ
	int getNumero();

	// recupere le type du champ ("text" >> donnees numeriques ,"combo" >> liste de choix ,"check" >> donnees booleennes)
	int getType();

	// retourne la valeur du champ >> toujours en double de maniere a simplifier le traitement
	Object getValue();
	
	// affectation du champ >> toujours en double pour la meme raison
	void setValue(Object val);
	
	// pour pouvoir appeller certaines fonctions de JComponent
	// sur un objet implementant Param sans pour autant specifier sa classe ;
	void setLocation(Point p);

	void addKeyListener(KeyListener kl);

	void addMouseListener(MouseListener ml);

	void addFocusListener(FocusListener ml);

	void addItemListener(ItemListener il);

	void setEnabled(boolean b);

	void requestFocus();
}

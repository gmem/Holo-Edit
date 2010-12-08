/*
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
import holoedit.data.HoloWaveForm;
import holoedit.data.HoloSDIFdata;

import java.awt.*;
import javax.swing.*;
import javax.swing.tree.*;

/** permet de charger dans les JTree des icones différentes selon les types de fichiers. */
public class IconNodeRenderer extends DefaultTreeCellRenderer {

  public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf,
      int row, boolean hasFocus)
  {
	super.getTreeCellRendererComponent(tree, value,
	sel, expanded, leaf, row, hasFocus);


	DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
	if (!node.isRoot())
		try {
			HoloWaveForm hwf = (HoloWaveForm) node.getUserObject();
			setIcon(new ImageIcon("images/wave.gif"));
		}catch(ClassCastException cce){
			try {
				HoloSDIFdata xtdt = (HoloSDIFdata) node.getUserObject();
				if (!xtdt.getDataType().equalsIgnoreCase("SDIF")) // drag du plus haut pere interdit
					setIcon(this.leafIcon);
			}catch(ClassCastException cce2){
				
				System.err.println("pb while loading node icon");
			}
		}
	return this;
  }
}


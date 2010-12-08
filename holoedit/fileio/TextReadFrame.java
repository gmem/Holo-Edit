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
package holoedit.fileio;

import javax.swing.*;
import java.awt.event.*;

public class TextReadFrame extends JFrame
{
	JTextArea textArea = new JTextArea(100, 100);
	JScrollPane scrollPane = new JScrollPane(textArea, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

	// constructeur par defaut
	public TextReadFrame(String nomFichier, String contenuFichier)
	{
		super(nomFichier);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		textArea.setText(contenuFichier);
		getContentPane().add(scrollPane);
		this.setSize(300, 300);
		setVisible(true);
		this.setLocation(362, 234);
		this.addWindowListener(new WindowAdapter()
		{
			public void windowDeactivated(WindowEvent e)
			{
				e.getWindow().dispose();
			}
		});
	}
}

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

/*
 */
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.MouseListener;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

public class DynamicTree extends JPanel
{
	protected DefaultMutableTreeNode rootNode;
	protected DefaultTreeModel treeModel;
	protected DefaultMutableTreeNode selectedNode;
	private JTree tree;
	private Toolkit toolkit = Toolkit.getDefaultToolkit();

	public DynamicTree(String s, boolean allowMultipleSelection)
	{
		super(new GridLayout(1, 0));
		rootNode = new DefaultMutableTreeNode(s);
		treeModel = new DefaultTreeModel(rootNode);
		treeModel.addTreeModelListener(new MyTreeModelListener());
		tree = new JTree(treeModel);
		tree.setCellRenderer(new IconNodeRenderer());
		tree.setEditable(false);
		tree.setRequestFocusEnabled(false);
		if (allowMultipleSelection == false)
			tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		else if (allowMultipleSelection == true)
			tree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
		tree.setShowsRootHandles(true);
		tree.setDragEnabled(true);
		tree.setFocusable(true);
		JScrollPane scrollPane = new JScrollPane(tree);
		scrollPane.setBorder(BorderFactory.createLoweredBevelBorder());
		add(scrollPane);
	}
	/** Remove all nodes except the root node. */
	public void clear()
	{
		rootNode.removeAllChildren();
		treeModel.reload();
	}

	/** Remove the currently selected node. */
	public void removeCurrentNode()
	{
		TreePath currentSelection = tree.getSelectionPath();
		if (currentSelection != null)
		{
			DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode) (currentSelection.getLastPathComponent());
			MutableTreeNode parent = (MutableTreeNode) (currentNode.getParent());
			if (parent != null)
			{
				treeModel.removeNodeFromParent(currentNode);
				return;
			}
		}
		// Either there was no selection, or the root was selected.
		toolkit.beep();
	}

	/** Add child to the currently selected node. */
	public DefaultMutableTreeNode addObject(Object child)
	{
		DefaultMutableTreeNode parentNode = null;
	//	TreePath parentPath = tree.getSelectionPath();
	//	if (parentPath == null)
//		{
			parentNode = rootNode;
	//	}
	//	else
	//	{
	//		parentNode = (DefaultMutableTreeNode) (parentPath.getLastPathComponent());
	//	}
		return addObject(parentNode, child, true);
	}

	public DefaultMutableTreeNode addObject(DefaultMutableTreeNode parent, Object child)
	{
		return addObject(parent, child, false);
	}

	public DefaultMutableTreeNode addObject(DefaultMutableTreeNode parent, Object child, boolean shouldBeVisible)
	{
		DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(child);
		if (parent == null)
		{
			parent = rootNode;
		}
/*		if (parent.getPath()==null){
			
		}*/
		treeModel.insertNodeInto(childNode, parent, parent.getChildCount());
		// Make sure the user can see the lovely new node.
		if (shouldBeVisible)
		{
			tree.scrollPathToVisible(new TreePath(childNode.getPath()));
		}
		return childNode;
	}
	
	
	public TreePath[] getSelectionPaths(){
		return tree.getSelectionPaths();
	}

	class MyTreeModelListener implements TreeModelListener
	{
		public void treeNodesChanged(TreeModelEvent e)
		{
			selectedNode = (DefaultMutableTreeNode) (e.getTreePath().getLastPathComponent());
		
			/*
			 * If the event lists children, then the changed node is the child of the node we've already gotten. Otherwise, the changed node and the specified node are the same.
			 */
			try
			{
				int index = e.getChildIndices()[0];
				selectedNode = (DefaultMutableTreeNode) (selectedNode.getChildAt(index));
			}
			catch (NullPointerException exc)
			{
			}
			System.out.println("The user has finished editing the node.");
			System.out.println("New value: " + selectedNode.getUserObject());
		}

		public void treeNodesInserted(TreeModelEvent e)
		{
		}

		public void treeNodesRemoved(TreeModelEvent e)
		{
		}

		public void treeStructureChanged(TreeModelEvent e)
		{
		}
	}

	public void add(JPopupMenu popup)
	{
		tree.add(popup);
	}

	public void addMouseListener(MouseListener ml)
	{
		tree.addMouseListener(ml);
	}

	public void addTreeSelectionListener(TreeSelectionListener tsl)
	{
		tree.addTreeSelectionListener(tsl);
	}

	public DefaultMutableTreeNode getSelectedNode()
	{
		return selectedNode;
	}

	public Object getLastSelectedPathComponent()
	{
		return tree.getLastSelectedPathComponent();
	}

	public JTree getTree()
	{
		return tree;
	}
	public void addSelection(TreePath path){
		tree.getSelectionModel().addSelectionPath(path);
	}	
	public void removeSelection(TreePath path){
		tree.getSelectionModel().addSelectionPath(path);
	}
}

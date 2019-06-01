package model;

import javax.swing.event.EventListenerList;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.io.File;
import java.util.ArrayList;
import java.util.Vector;

/**
 * Author: Sean
 * Date: Created In 12:34 2019/5/30
 * Title:
 * Description:
 * Version: 0.1
 * Update History:
 * [Date][Version][Author] What has been done;
 */

public class STreeModel implements TreeModel {
    private SFileNode root = null;
    private EventListenerList eventListenerList = new EventListenerList();

    public STreeModel(){

    }

    public STreeModel(SFileNode root){
        this.root = root;
    }

    public void setRoot(SFileNode sFile){
        root = sFile;
        fireTreeStructureChanged(sFile);
    }

    @Override
    public Object getRoot() {
        return root;
    }

    public Object getParent(Object node){
        return new SFileNode(((SFileNode)node).getFile().getParentFile());
    }

    @Override
    public Object getChild(Object parent, int index) {
        return ((SFileNode)parent).getDirChildAt(index);
    }

    @Override
    public int getChildCount(Object parent) {
        return ((SFileNode)parent).getDirChildCount();
    }


    @Override
    public boolean isLeaf(Object node) {
        return ((SFileNode)node).isDirLeaf();
    }

    @Override
    public void valueForPathChanged(TreePath path, Object newValue) {
    }

    @Override
    public int getIndexOfChild(Object parent, Object child) {
        return ((SFileNode)parent).getDirIndex((STreeNode)child);
    }

    @Override
    public void addTreeModelListener(TreeModelListener l) {
        eventListenerList.add(TreeModelListener.class, l);
    }

    @Override
    public void removeTreeModelListener(TreeModelListener l) {
        eventListenerList.remove(TreeModelListener.class, l);
    }


    private void fireTreeStructureChanged(Object oldRoot){
        TreeModelEvent event = new TreeModelEvent(this, new Object[]{oldRoot});
        for(TreeModelListener l : eventListenerList.getListeners(TreeModelListener.class)){
            l.treeStructureChanged(event);
        }
    }

    public String toString(){
        return root.toString();
    }
}

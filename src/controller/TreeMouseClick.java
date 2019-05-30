package controller;

import model.SFileNode;
import view.MainFrame;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import static view.MainFrame.filesTable;
import static view.MainFrame.useFileHiding;

/**
 * Author: Sean
 * Date: Created In 22:22 2019/5/30
 * Title:
 * Description:
 * Version: 0.1
 * Update History:
 * [Date][Version][Author] What has been done;
 */

public class TreeMouseClick extends MouseAdapter {
    @Override
    public void mouseClicked(MouseEvent e) {
        if(SwingUtilities.isLeftMouseButton(e)){
            TreePath path = MainFrame.contentTree.getSelectionPath();
            if(path == null) return;
            SFileNode sFileNode = (SFileNode)path.getLastPathComponent();
            filesTable.getSelectionModel().clearSelection();
            filesTable.setTable(sFileNode, useFileHiding);
        }
    }
}

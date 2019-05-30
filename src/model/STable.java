package model;

import view.MainFrame;

import javax.swing.*;

/**
 * Author: Sean
 * Date: Created In 17:06 2019/5/30
 * Title:
 * Description:
 * Version: 0.1
 * Update History:
 * [Date][Version][Author] What has been done;
 */

public class STable extends JTable {
    private STableModel sTableModel;

    public STable(STableModel sTableModel){
        this.sTableModel = sTableModel;
        setModel(sTableModel);

    }

    public void setTable(SFileNode node, boolean useFileHiding){
        sTableModel.setsFileNode(node, useFileHiding);
        setModel(sTableModel);
        updateUI();
        MainFrame.setTfPath(node.getPath());
    }

    public STableModel getModel(){
        return sTableModel;
    }
}

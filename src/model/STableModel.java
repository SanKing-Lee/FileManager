package model;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Vector;

/**
 * Author: Sean
 * Date: Created In 14:43 2019/5/30
 * Title:
 * Description:
 * Version: 0.1
 * Update History:
 * [Date][Version][Author] What has been done;
 */

public class STableModel extends AbstractTableModel {
    private int rowSize;
    private int colSize;
    private final String[] colName = {"名称", "修改日期", "类型", "大小"/*, "隐藏"*/};
    private boolean useFileHiding = true;
    private SFileNode sFileNode;
    private Vector<File> children;
    private final FileSystemView fileSystemView = FileSystemView.getFileSystemView();

    public STableModel(SFileNode sFileNode, boolean useFileHiding) {
        this.useFileHiding = useFileHiding;
        this.sFileNode = sFileNode;
        update();
    }

    public void setsFileNode(SFileNode sFileNode, boolean useFileHiding) {
        this.sFileNode = sFileNode;
        this.useFileHiding = useFileHiding;
        update();
    }

    private void update() {
        rowSize = sFileNode.getFilesCount(useFileHiding);
        colSize = colName.length;
        children = sFileNode.getFiles(useFileHiding);
    }

    private String getLastModified(File file) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(file.lastModified());
        return sdf.format(cal.getTime());
    }

    @Override
    public int getRowCount() {
        return rowSize;
    }

    @Override
    public int getColumnCount() {
        return colSize;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (rowIndex >= children.size()) {
            return null;
        }
        File child = children.get(rowIndex);

        switch (columnIndex) {
            case -1:
                return new SFileNode(child);
            case 0:
                return new JLabel(fileSystemView.getSystemDisplayName(child),
                        fileSystemView.getSystemIcon(child), SwingConstants.LEFT);
            case 1:
                return getLastModified(child);
            case 2:
                return fileSystemView.getSystemTypeDescription(child);
            case 3:
                return (child.isDirectory()) ? null : (child.length() + "KB");
//            case 3: return child.isHidden()?"是":"";
            default:
                return null;
        }
    }

    @Override
    public String getColumnName(int column) {
        return colName[column];
    }

    public SFileNode getsFileNode() {
        return sFileNode;
    }
}

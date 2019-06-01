package model;

import javax.swing.*;
import java.io.File;
import java.util.Vector;

/**
 * Author: Sean
 * Date: Created In 17:29 2019/5/30
 * Title:
 * Description:
 * Version: 0.1
 * Update History:
 * [Date][Version][Author] What has been done;
 */

public interface STreeNode {
    public STreeNode getParent();
    public File getFile();
    public Icon getIcon();
    public String getPath();
    public long getFileSize();
    public String getLastModified();


    //****************************************************for tree****************************************************//
    // 用于在目录树显示中需要的API
    public STreeNode getDirChildAt(int index);
    public int getDirIndex(STreeNode child);
    public int getDirChildCount();
    public boolean isDirLeaf();

    //****************************************************for table****************************************************//
    // 用于在文件表显示中需要的API
    public int getFilesCount(boolean useFileHiding);
    public Vector<File> getFiles(boolean useFileHiding);

    // 用于操作文件需要的API

}

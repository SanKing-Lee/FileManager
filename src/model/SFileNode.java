package model;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import javax.swing.tree.TreeNode;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Stack;
import java.util.Vector;

/**
 * Author: Sean
 * Date: Created In 13:25 2019/5/30
 * Title:
 * Description:
 * Version: 0.1
 * Update History:
 * [Date][Version][Author] What has been done;
 */

public class SFileNode implements STreeNode {
    private File file;
    private FileSystemView fileSystemView = FileSystemView.getFileSystemView();
    private Vector<File> files = new Vector<>();
    private Vector<File> directories = new Vector<>();
    private Vector<File> unhiddenFiles = new Vector<>();
    private boolean record = false;

    public SFileNode() {
        this.file = fileSystemView.getHomeDirectory();
        addFiles();
    }

    public SFileNode(File file) {
        this.file = file;
        addFiles();
    }

    private boolean isDirectory(File file) {
        return (file.isDirectory()
                && !(file.getName().toLowerCase().endsWith(".lnk")));
    }

    public void add(File file) {
        files.add(file);
    }

    private void addFiles() {
        // 将该文件下的所有文件添加到该结点的子结点中
        File[] fileList = fileSystemView.getFiles(file, false);
        for (File childFile : fileList) {
            // 如果这不是一个隐藏文件，则把它添加至未隐藏文件和目录
            if (!childFile.isHidden()) {
                unhiddenFiles.add(childFile);
            }
            // 将该子结点添加至孩子结点中
            files.add(childFile);
            // 如果这个子结点是一个目录文件，则将其添加至目录列表中
            if (isDirectory(childFile)) {
                directories.add(childFile);
            }
        }
    }

    @Override
    public STreeNode getParent() {
        File parentFile = file.getParentFile();
        if(parentFile == null){
            return null;
        }
        return new SFileNode(parentFile);
    }

    @Override
    public STreeNode getDirChildAt(int index) {
        return new SFileNode(directories.get(index));
    }

    @Override
    public int getDirIndex(STreeNode child) {
        for (int i = 0; i < directories.size(); i++) {
            if (child.getFile().equals(directories.get(i))) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public int getDirChildCount() {
        return directories.size();
    }

    @Override
    public boolean isDirLeaf() {
        return directories.size() == 0;
    }

    @Override
    public Icon getIcon() {
        return fileSystemView.getSystemIcon(file);
    }

    @Override
    public String getPath() {
        return file.getPath();
    }

    @Override
    public long getFileSize() {
        return (isDirectory(file)) ? getDirSize(file) : file.length();
    }

    private long getDirSize(File file) {
        long size = 0;
        Stack<File> dirStack = new Stack<>();
        dirStack.push(file);
        while (!dirStack.empty()) {
            File currFile = dirStack.pop();
            File[] fileList = fileSystemView.getFiles(currFile, true);
            for (File nextFile : fileList) {
                if (isDirectory(nextFile)) {
                    dirStack.push(nextFile);
                } else {
                    size += nextFile.length();
                }
            }
        }
        return size;
    }

    public void removeFile(File file) {
        files.remove(file);
    }

    public void removeAllFiles() {
        files.removeAllElements();
    }

    @Override
    public String getLastModified() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(file.lastModified());
        return sdf.format(cal.getTime());
    }

    @Override
    public int getFilesCount(boolean useFileHiding) {
        return useFileHiding ? unhiddenFiles.size() : files.size();
    }

    @Override
    public Vector<File> getFiles(boolean useFileHiding) {
        return useFileHiding ? unhiddenFiles : files;
    }

    @Override
    public File getFile() {
        return file;
    }

    public String toString() {
        return fileSystemView.getSystemDisplayName(file);
    }

    public boolean isRecord() {
        return record;
    }

    public void setRecord(boolean record) {
        this.record = record;
    }
}

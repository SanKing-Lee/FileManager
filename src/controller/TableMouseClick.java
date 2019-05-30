package controller;

import model.SFileNode;
import view.MainFrame;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.Date;

/**
 * Author: Sean
 * Date: Created In 22:02 2019/5/30
 * Title:
 * Description:
 * Version: 0.1
 * Update History:
 * [Date][Version][Author] What has been done;
 */

public class TableMouseClick extends MouseAdapter {
    private long preClickTime = 0;

    @Override
    public void mouseClicked(MouseEvent e) {
        if(SwingUtilities.isLeftMouseButton(e)){
            if(isDoubleClick()){
//                System.out.println("double click");
                int index = MainFrame.filesTable.getSelectedRow();
                SFileNode node = (SFileNode)MainFrame.filesTable.getValueAt(index, -1);
                File file = node.getFile();
                //如果是文件就直接打开
                if (file.isFile()) {
                    Runtime run = Runtime.getRuntime();//每个 Java 应用程序都有一个 Runtime 类实例，使应用程序能够与其运行的环境相连接。
                    //可以通过 getRuntime 方法获取当前运行时。
                    try {//在单独的进程中执行指定的字符串命令。
                        //这是一个很有用的方法。对于 exec(command) 形式的调用而言，其行为与调用 exec(command, null, null) 完全相同
                        Process process = run.exec("cmd /c call " + "\""
                                + file.getPath() + "\"");//调用系统命令执行文件
                    } catch (IOException e1) {
                        e1.printStackTrace();
                        System.exit(0);
                    }
                }
                //其他就显示该文件夹中的文件
                else {
                    MainFrame.filesTable.getSelectionModel().clearSelection();
                    MainFrame.filesTable.setTable(node, MainFrame.useFileHiding);  //显示该节点下的文件节点
                }
            }
        }
    }

    private boolean isDoubleClick() {
        long curClickTime = new Date().getTime();
        if (curClickTime - preClickTime < 300) {
            return true;
        }
        preClickTime = curClickTime;
        return false;
    }
}

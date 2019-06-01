import controller.Controller;
import view.MainFrame;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.io.File;

/**
 * Author: Sean
 * Date: Created In 14:39 2019/5/21
 * Title:
 * Description:
 * Version: 0.1
 * Update History:
 * [Date][Version][Author] What has been done;
 */

public class FileManager {
    public static void main(String[] args){
        EventQueue.invokeLater(()->{
            JFrame frame = new MainFrame();
            frame.setTitle("FileManager");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
        });
    }
}

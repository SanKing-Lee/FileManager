package controller;

import model.SFileNode;
import model.STreeModel;
import view.MainFrame;

import javax.swing.filechooser.FileSystemView;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Author: Sean
 * Date: Created In 16:56 2019/5/21
 * Title:
 * Description:
 * Version: 0.1
 * Update History:
 * [Date][Version][Author] What has been done;
 */

public class Controller {
    // search
    private static final int FILE_QUEUE_SIZE = 50;
    private final int ENUMERATORS = 10;
    private final int SEARCH_THREADS = 10;
    private static final File DONE = new File("");
    private static BlockingQueue<File> queue = new ArrayBlockingQueue<>(FILE_QUEUE_SIZE);
    private static boolean Finish = false;
    private SFileNode gotNode =  new SFileNode(new File("search"));
    private static Vector<File> gotFiles = new Vector<>();

    public void search(String keyword){
//        MainFrame.filesTable.setTable(gotNode, MainFrame.useFileHiding);
        for(int i = 0; i < ENUMERATORS; i++) {
            Runnable enumerator = () -> {
                try {
                    enumerate((MainFrame.filesTable.getModel()).getsFileNode().getFile());
                    queue.put(DONE);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            };
            new Thread(enumerator).start();
        }
        for(int i = 0; i < SEARCH_THREADS; i++){
            Runnable searchFile = ()->{
                try{
                    boolean done = false;
                    while(!done){
                        File file = queue.take();
                        if(file == DONE){
                            queue.put(file);
                            done = true;
                            Finish = true;
                        }
                        else searchFile(file, keyword);
                    }
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            };
            new Thread(searchFile).start();
        }
        while(!Finish){
//            System.out.println("Searching");
        }
        System.out.println("DONE!");
        for(File file : gotFiles){
            gotNode.add(file);
        }
        gotFiles.removeAllElements();
        MainFrame.filesTable.setTable(gotNode, MainFrame.useFileHiding);
    }

    private void enumerate(File directory){
        File[] files = directory.listFiles();
        if(files == null){
            return;
        }
        try {
            for (File file : files) {
                if (file.isDirectory() && !file.getName().endsWith(".lnk")) {
                    enumerate(file);
                }
                queue.put(file);
            }
        }catch (InterruptedException e){
            e.printStackTrace();
        }
    }

    private void searchFile(File file, String keyword){
        String name = file.getName();
        System.out.println(file.getPath());
        if(name.contains(keyword)){
            gotFiles.add(file);
        }
    }
}

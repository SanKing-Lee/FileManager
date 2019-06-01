package controller;

import model.SFileNode;
import view.MainFrame;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import javax.swing.tree.TreePath;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static view.MainFrame.filesTable;
import static view.MainFrame.useFileHiding;

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
    public void message(String msg) {
        JOptionPane.showMessageDialog(filesTable, msg);
    }

    //--------------------------------------------- double click -----------------------------------------------------//
    public TableMouseClick getTableMouseClick() {
        return new TableMouseClick();
    }

    class TableMouseClick extends MouseAdapter {
        private long preClickTime = 0;

        @Override
        public void mouseClicked(MouseEvent e) {
            if (SwingUtilities.isLeftMouseButton(e)) {
                if (isDoubleClick()) {
                    MainFrame.openFile();
                }
            }
            if (SwingUtilities.isRightMouseButton(e)) {
                if (MainFrame.filesTable.getSelectionModel().getMinSelectionIndex() > 0) {
                    MainFrame.showFilesPaneRightClickMenu(e.getX(), e.getY());
                } else {
                    MainFrame.showFilesPaneRightClickMenu(e.getX(), e.getY());
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

    public TreeMouseClick getTreeMouseClick() {
        return new TreeMouseClick();
    }

    class TreeMouseClick extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            if (SwingUtilities.isLeftMouseButton(e)) {
                TreePath path = MainFrame.contentTree.getSelectionPath();
                if (path == null) return;
                SFileNode sFileNode = (SFileNode) path.getLastPathComponent();
                MainFrame.update(sFileNode);
            }
        }
    }


    //--------------------------------------------------- search -----------------------------------------------------//
    private SFileNode gotNode = new SFileNode(new File("search"));
    private Vector<File> gotFiles;

    public SFileNode search(File file, String keyword) {
        MainFrame.update(gotNode);
        MatchFile matchFile = new MatchFile(file, keyword);
        FutureTask<Vector<File>> task = new FutureTask<>(matchFile);
        new Thread(task).start();
        try {
            gotFiles = task.get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        gotNode.removeAllFiles();
        for (File searchedFile : gotFiles) {
            gotNode.add(searchedFile);
        }
        gotFiles.removeAllElements();
        System.out.println("DONE!");
        return gotNode;
    }

    class MatchFile implements Callable<Vector<File>> {
        private File directory;
        private String keyword;

        private MatchFile(File directory, String keyword) {
            this.directory = directory;
            this.keyword = keyword;
        }

        public Vector<File> call() {
            Vector<File> searchedFiles = new Vector<>();
            try {
                File[] files = directory.listFiles();
                List<Future<Vector<File>>> results = new ArrayList<>();
                if (files == null) {
                    return searchedFiles;
                }
                for (File file : files) {
                    if (file.isDirectory()) {
                        MatchFile matchFile = new MatchFile(file, keyword);
                        FutureTask<Vector<File>> task = new FutureTask<>(matchFile);
                        results.add(task);
                        new Thread(task).start();
                    }
                    if (searchFile(file)) {
                        searchedFiles.add(file);
                    }
                }
                for (Future<Vector<File>> result : results) {
                    try {
                        searchedFiles.addAll(result.get());
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return searchedFiles;
        }

        boolean searchFile(File file) {
            String name = file.getName();
            System.out.println(file.getPath());
            return name.contains(keyword);
        }
    }


    //--------------------------------------------- backward and forward ---------------------------------------------//
    private static Vector<SFileNode> recordVector = new Vector<>();
    private static int recordPos = -1;

    public static void addRecord(SFileNode node) {
        if (!node.isRecord()) {
            // 当前位置不在添加新节点的位置
            if (recordPos != recordVector.size() - 1) {
                recordVector.subList(recordPos + 1, recordVector.size()).clear();
//                recordPos = recordVector.size()-1;
            }
            // 正常的添加新节点情况
            node.setRecord(true);
            recordVector.add(node);
            recordPos++;
        }

    }

    public static SFileNode backward() {
        if (recordPos > 0) {
            recordPos--;
        }
        return recordVector.get(recordPos);
    }

    public static SFileNode forward() {
        if (recordPos < recordVector.size() - 1) {
            recordPos++;
        }
        return recordVector.get(recordPos);
    }

    //-------------------------------------------- actionListeners ---------------------------------------------------//
    private static final Vector<String> clipboard = new Vector<>();
    private String sFromPath;
    private static boolean copy = true;

    private File getNodeFile() {
        return MainFrame.filesTable.getModel().getsFileNode().getFile();
    }

    private SFileNode getSFileNode() {
        return MainFrame.filesTable.getModel().getsFileNode();
    }

    private File getSelectedFile() {
        if (MainFrame.filesTable.getSelectedRowCount() > 1) {
            JOptionPane.showMessageDialog(MainFrame.filesTable, "一次只能选择一个文件！");
            return null;
        }
        int selectedRow = MainFrame.filesTable.getSelectedRow();
        SFileNode selectedNode = (SFileNode) MainFrame.filesTable.getValueAt(selectedRow, -1);
        return selectedNode.getFile();
    }

    private File[] getSelectedFiles() {
        int[] selectedRows = filesTable.getSelectedRows();
        File[] selectedFiles = new File[selectedRows.length];
        for (int i = 0; i < selectedFiles.length; i++) {
            selectedFiles[i] = ((SFileNode) filesTable.getValueAt(selectedRows[i], -1)).getFile();
        }
        return selectedFiles;
    }

    // open file
    public OpenFileListener getOpenFileListener() {
        return new OpenFileListener();
    }

    class OpenFileListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            MainFrame.openFile();
        }
    }

    // new file
    public NewFileListener getNewFileListener(String suffix) {
        return new NewFileListener(suffix);
    }

    class NewFileListener implements ActionListener {
        private String suffix;
        private String prefix = null;

        NewFileListener(String suffix) {
            this.suffix = suffix;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            File nodeFile = getNodeFile();
            setPrefix();
            Path path = Paths.get(nodeFile.getPath(), prefix + suffix);
            File file = path.toFile();
            int count = 1;
            while (file.exists()) {
                setPrefix();
                prefix += "(" + count + ")";
                path = Paths.get(nodeFile.getPath(), prefix + suffix);
                file = path.toFile();
                count++;
            }
            try {
                Files.createFile(path);
            } catch (IOException exc) {
                exc.printStackTrace();
            }
            MainFrame.refresh();
        }

        private void setPrefix() {
            switch (suffix) {
                case ".txt":
                    prefix = "新建文本文档";
                    break;
                case ".word":
                    prefix = "新建Word文件";
                    break;
                case ".pdf":
                    prefix = "新建PDF文件";
                    break;
                default:
                    prefix = "新建文件";
            }
        }
    }

    // new directory
    public NewDirListener getNewDirListener() {
        return new NewDirListener();
    }

    class NewDirListener implements ActionListener {
        String name = "新建文件夹";

        @Override
        public void actionPerformed(ActionEvent e) {
            File nodeFile = getNodeFile();
            Path path = Paths.get(nodeFile.getPath(), name);
            File file = path.toFile();
            int dirCount = 1;
            while (file.exists()) {
                name = "新建文件夹(" + dirCount++ + ")";
                path = Paths.get(nodeFile.getPath(), name);
                file = path.toFile();
            }
            try {
                Files.createDirectories(path);
            } catch (IOException exc) {
                exc.printStackTrace();
            }
            MainFrame.refresh();
        }
    }

    // delete file
    public DeleteFileListener getDeleteFileListener() {
        return new DeleteFileListener();
    }

    class DeleteFileListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            int[] selectedRows = MainFrame.filesTable.getSelectedRows();
            for (int index : selectedRows) {
                SFileNode selectedNode = (SFileNode) MainFrame.filesTable.getValueAt(index, -1);
                File file = selectedNode.getFile();
                if (file.isDirectory()) {
                    deleteDir(file);
                } else {
                    deleteFile(file);
                }
            }
            MainFrame.refresh();
        }

        void deleteFile(File file) {
            Path path = Paths.get(file.getPath());
            try {
                Files.delete(path);
            } catch (IOException exc) {
                message("删除文件 " + file.getName() + " 时出错！");
                exc.printStackTrace();
            }
        }

        void deleteDir(File dir) {
            Stack<File> dirStack = new Stack<>();
            dirStack.push(dir);
            Stack<File> emptyDirStack = new Stack<>();
            while (!dirStack.empty()) {
                File curDir = dirStack.pop();
                File[] files = curDir.listFiles();
                emptyDirStack.push(curDir);
                if (files == null || files.length == 0) {
                    continue;
                }
                for (File file : files) {
                    if (file.isDirectory()) {
                        dirStack.push(file);
                    } else {
                        deleteFile(file);
                    }
                }
            }
            while (!emptyDirStack.empty()) {
                deleteFile(emptyDirStack.pop());
            }
        }
    }

    // rename file
    public RenameFileListener getRenameFileListener() {
        return new RenameFileListener();
    }

    class RenameFileListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (MainFrame.filesTable.getSelectedRowCount() > 1) {
                JOptionPane.showMessageDialog(MainFrame.filesTable, "一次只能选择一个文件！");
                return;
            }
            int selectedRow = MainFrame.filesTable.getSelectedRow();
            SFileNode selectedNode = (SFileNode) MainFrame.filesTable.getValueAt(selectedRow, -1);
            String newName = JOptionPane.showInputDialog("请输入新名称", selectedNode.getFile().getName());
            if (newName == null) {
                return;
            }
            if (!selectedNode.getFile().renameTo(new File(getNodeFile().getPath(), newName))) {
                JOptionPane.showMessageDialog(MainFrame.filesTable, "重命名失败！");
            }
            MainFrame.refresh();
        }
    }

    // copy file
    private void addToClipboard() {
        clipboard.removeAllElements();
        int[] selectedRows = MainFrame.filesTable.getSelectedRows();
        SFileNode node = filesTable.getModel().getsFileNode();
        sFromPath = node.getPath();
        for (int index : selectedRows) {
            SFileNode selectedNode = (SFileNode) MainFrame.filesTable.getValueAt(index, -1);
            File file = selectedNode.getFile();
            if (file.isDirectory()) {
                addDirToClipboard(file);
            } else {
                addFileToClipboard(file);
            }
        }
    }

    private Path getToPath(File file, Path fromPath, Path toPath) {
        String path = file.getPath();
        return Paths.get(toPath.toString() + path.substring(path.lastIndexOf('\\')) + fromPath.toString().substring(path.length()));
    }

    private void addFileToClipboard(File file) {
        Path path = Paths.get(file.getPath());
        clipboard.add(path.toString());
    }

    private void addDirToClipboard(File dir) {
        Stack<File> dirStack = new Stack<>();
        dirStack.push(dir);
        while (!dirStack.empty()) {
            File curDir = dirStack.pop();
            addFileToClipboard(curDir);
            File[] files = curDir.listFiles();
            if (files == null) {
                continue;
            }
            for (File file : files) {
                if (file.isDirectory()) {
                    dirStack.push(file);
                } else {
                    addFileToClipboard(file);
                }
            }
        }
    }

    public CopyFileListener getCopyFileListener() {
        return new CopyFileListener();
    }

    class CopyFileListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            addToClipboard();
            copy = true;
        }
    }

    // cut file
    public CutFileListener getCutFileListener() {
        return new CutFileListener();
    }

    class CutFileListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            addToClipboard();
            copy = false;
        }
    }

    // paste file
    public PasteFieListener getPasteFileListener() {
        return new PasteFieListener();
    }

    class PasteFieListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            SFileNode node = getSFileNode();
            String sTargetPath = node.getPath();
            if (clipboard.size() == 0) {
                return;
            }
            for (String filePath : clipboard) {
                String relativePath = filePath.substring(sFromPath.length());
                Path toPath = Paths.get(sTargetPath, relativePath);
                Path fromPath = Paths.get(filePath);
                try {
                    if (copy) {
                        Files.copy(fromPath, toPath);
                    } else {
                        Files.move(fromPath, toPath);
                    }
                } catch (IOException exc) {
                    JOptionPane.showMessageDialog(filesTable, "粘贴文件" + fromPath + "失败！请重试！");
                }
            }
            clipboard.removeAllElements();
            MainFrame.refresh();
        }
    }


    // zip file
    public ZipFileListener getZipFileListener() {
        return new ZipFileListener();
    }

    class ZipFileListener implements ActionListener {
        String zipFileName;
        File nodeFile;

        @Override
        public void actionPerformed(ActionEvent e) {
            File[] files = getSelectedFiles();
            nodeFile = getNodeFile();
            String name = JOptionPane.showInputDialog(filesTable, "请输入压缩后的文件名称", nodeFile.getName());
            if (name == null) {
                return;
            }
            zipFileName = getSFileNode().getPath() + "\\" + name + ".zip";
            System.out.println(zipFileName);
            try {
                ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFileName));
                for (File file : files) {
                    if (file.isDirectory()) {
                        zipDir(file, zos);
                    } else {
                        zipFile(file, zos, file.getName());
                    }
                }
                zos.close();
            } catch (IOException exec) {
                message("创建压缩文件 " + zipFileName + " 出错！");
            }
            System.out.println("压缩完成！");
            MainFrame.refresh();
        }

        private void zipFile(File srcFile, ZipOutputStream zos, String zipName) {
            try {
                System.out.println("压缩文件 " + zipName + " 中...");
                FileInputStream fis = new FileInputStream(srcFile);
                BufferedInputStream bis = new BufferedInputStream(fis);
                // 将文件相对于当前目录的相对路径作为入口名称
                zos.putNextEntry(new ZipEntry(zipName));
                int b;
                System.out.println("压缩中...");
                while ((b = bis.read()) != -1) {
                    zos.write(b);
                }
                zos.closeEntry();
                System.out.println("压缩文件 " + zipName + " 完成！");
            } catch (FileNotFoundException exc) {
                message("未找到文件 " + zipName + " !");
            } catch (IOException exc) {
                message("读取文件 " + zipName + " 出错！");
            }
        }

        private void zipDir(File dir, ZipOutputStream zos) {
            Stack<File> fileStack = new Stack<>();
            String name = "";
            fileStack.push(dir);
            while (!fileStack.isEmpty()) {
                File curDir = fileStack.pop();
                name += curDir.getName() + "\\";
                File[] files = curDir.listFiles();
                // 空文件夹
                try {
                    zos.putNextEntry(new ZipEntry(name));
                    zos.closeEntry();
                    if (files == null) {
                        continue;
                    }
                    // 遍历文件
                    for (File file : files) {
                        // 如果是目录，放入栈中
                        if (file.isDirectory()) {
                            fileStack.push(file);
                        }
                        // 如果是文件，进行压缩
                        else {
                            zipFile(file, zos, name + file.getName());
                        }
                    }
                } catch (IOException e) {
                    message("创建文件夹 " + curDir.getName() + "失败！");
                }
            }
        }
    }

    // unzip file
    public UnzipFileListener getUnzipFileListener() {
        return new UnzipFileListener();
    }

    class UnzipFileListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            File zipFile = getSelectedFile();
            byte[] buffer = new byte[1024];
            if (zipFile == null) {
                return;
            }
            long startTime = System.currentTimeMillis();
            try {
                ZipInputStream Zin = new ZipInputStream(new FileInputStream(zipFile));//输入源zip路径
                BufferedInputStream Bin = new BufferedInputStream(Zin);
                File parent = getNodeFile();
                Path dir = Files.createDirectory(Paths.get(parent.getPath(), zipFile.getName().substring(0, zipFile.getName().length() - 4)));
                ZipEntry entry;
                while ((entry = Zin.getNextEntry()) != null && !entry.isDirectory()) {
                    String sPath = dir.toString() + "\\" + entry.getName();
                    Path path = Paths.get(sPath);
                    if(sPath.endsWith("\\")){
                        Files.createDirectory(path);
                        continue;
                    }
                    else{
                        Files.createFile(path);
                    }
                    File fileOut = path.toFile();
                    System.out.println(fileOut);
                    FileOutputStream out = new FileOutputStream(fileOut);
                    BufferedOutputStream bOut = new BufferedOutputStream(out);
                    int b;
                    while ((b = Bin.read()) != -1) {
                        bOut.write(b);
                    }
                    bOut.close();
                    out.close();
                    System.out.println(fileOut + "解压成功");
                }
                Bin.close();
                Zin.close();
            } catch (IOException exc) {
                exc.printStackTrace();
            }

            long endTime = System.currentTimeMillis();
            System.out.println("耗费时间： " + (endTime - startTime) + " ms");
            MainFrame.refresh();
        }
    }


    final int key = 0x101;

    private void transferFile(boolean encrypt) {
        SFileNode node = getSFileNode();
        File selectedFile = getSelectedFile();
        if (selectedFile == null) {
            return;
        }
        Path transferPath = Paths.get(node.getPath(),
                (encrypt) ? selectedFile.getName() + ".enc" :
                        selectedFile.getName().substring(0, selectedFile.getName().lastIndexOf('.')));
        File transferFile = transferPath.toFile();
        try {
            if (transferFile.exists()) {
                Files.delete(transferPath);
            }
            Files.createFile(transferPath);
            transferFile = transferPath.toFile();
            FileOutputStream fos = new FileOutputStream(transferFile);
            FileInputStream fis = new FileInputStream(selectedFile);
            int len;
            while ((len = fis.read()) != -1) {
                fos.write(len ^ key);
            }
        } catch (IOException exc) {
            message("创建文件失败！");
        }
        MainFrame.refresh();
    }

    // encrypt file
    public EncryptFileListener getEncryptFileListener() {
        return new EncryptFileListener();
    }

    class EncryptFileListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            transferFile(true);
        }
    }

    // decrypt file
    public DecryptFileListener getDecryptFileListener() {
        return new DecryptFileListener();
    }

    class DecryptFileListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            transferFile(false);
        }
    }
}

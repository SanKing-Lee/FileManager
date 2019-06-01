package view;

import controller.Controller;
import model.*;
import sun.applet.Main;

import javax.swing.*;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Dimension2D;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

/**
 * Author: Sean
 * Date: Created In 14:34 2019/5/21
 * Title:
 * Description:
 * Version: 0.1
 * Update History:
 * [Date][Version][Author] What has been done;
 */

public class MainFrame extends JFrame {
    private static final int DEFAULT_WIDTH = 800;
    private static final int DEFAULT_HEIGHT = 450;
    private static final int DEFAULT_FILES_TABLE_ROW_HEIGHT = DEFAULT_HEIGHT / 18;
    private static final int DEFAULT_FILES_TABLE_NAME_COLUMN_WIDTH = DEFAULT_WIDTH / 16 * 3;
    private static final int DEFAULT_FILE_TREE_LOCATION = DEFAULT_WIDTH / 4;
    public static boolean useFileHiding = false;
    private Controller controller = new Controller();
    public static HashMap<String, SFileNode> encryptedFileNode = new HashMap<>();

    public static JTree contentTree;                // 文件目录树
    public static STable filesTable;                // 文件表
    private static JScrollPane contentPane;
    private static JScrollPane filesPane;
    private STableModel sTableModel;                // 文件表模式
    private static JTextArea infoTextArea;                 // 信息文本
    private ListSelectionModel filesTableSelModel;  // 文件表选中模式
    private JPanel pFunc;
    private JButton bParentDirectory;               // 返回上级目录的按钮
    private JButton bBackward;                        // 返回按钮
    private JButton bForward;                       // 前进按钮
    private static JTextField tfPath;                       // 地址框
    private JButton bRefresh;                       // 刷新按钮

    private JTextField tfSearch;                     // 搜索框
    private boolean startSearchInput = false;
    private JButton bSearch;                        // 搜索按钮

    private static JMenuBar menuBar;
    private static JMenu mnFile;
    private static JMenu mnNew;
    private static JMenuItem miTxt;
    private static JMenuItem miDir;
    private static JMenuItem miOpen;

    private static JMenu mnEdit;
    private static JMenuItem miDelete;
    private static JMenuItem miCopy;
    private static JMenuItem miCut;
    private static JMenuItem miPaste;
    private static JMenuItem miRename;

    private static JMenuItem RCMIOpen;
    private static JMenu RCMNNew;
    private static JMenuItem RCMIDir;
    private static JMenuItem RCMITxt;
    private static JMenuItem RCMIDelete;
    private static JMenuItem RCMIRename;
    private static JMenuItem RCMICopy;
    private static JMenuItem RCMICut;
    private static JMenuItem RCMIPaste;
    private static JMenuItem RCMIZip;
    private static JMenuItem RCMIUnzip;
    private static JMenuItem RCMIEnCrypt;
    private static JMenuItem RCMIDecrypt;

    public static JPopupMenu rightClickMenu;


    public MainFrame() {
        setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension2D screen = toolkit.getScreenSize();
        setLocation((int) (screen.getWidth() - DEFAULT_WIDTH) / 2, (int) (screen.getHeight() - DEFAULT_HEIGHT) / 2);

        // 文件系统树结构
        SFileTreeRenderer sFileTreeRenderer = new SFileTreeRenderer();
        SFileNode root = new SFileNode();
        STreeModel model = new STreeModel(root);
        contentTree = new JTree(model);
        contentTree.setCellRenderer(sFileTreeRenderer);
        contentTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        // 文件浏览表
        STableRenderer sTableRenderer = new STableRenderer();
        sTableModel = new STableModel(root, useFileHiding);
        filesTable = new STable(sTableModel);
        filesTable.setShowGrid(false);
        filesTable.setRowHeight(DEFAULT_FILES_TABLE_ROW_HEIGHT);

        TableColumnModel tcl = filesTable.getColumnModel();
        TableColumn nameTabCol = tcl.getColumn(0);
        nameTabCol.setCellRenderer(sTableRenderer);
        nameTabCol.setPreferredWidth(DEFAULT_FILES_TABLE_NAME_COLUMN_WIDTH);

        // 设置选中模式
        filesTable.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        filesTableSelModel = filesTable.getSelectionModel();
        // 设置左下角提示信息
        filesTableSelModel.addListSelectionListener((e) -> {
            infoTextArea.setText("共" + sTableModel.getsFileNode().getFilesCount(useFileHiding)
                    + "个项目，选中" + filesTable.getSelectedRows().length + "个项目。");
            setItemState();
        });

        contentPane = new JScrollPane(contentTree);
        filesPane = new JScrollPane(filesTable);
        filesTable.addMouseListener(controller.getTableMouseClick());
        contentTree.addMouseListener(controller.getTreeMouseClick());

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, contentPane, filesPane);
        splitPane.setDividerLocation(DEFAULT_FILE_TREE_LOCATION);


        // 左下角提示信息
        infoTextArea = new JTextArea();

        int tfWidth = 200;
        // 功能面板区
        pFunc = new JPanel();
        pFunc.setLayout(null);
        // 按钮面板
        // 后退按钮
        bBackward = new JButton(getIcon("backward"));
        bBackward.setBounds(0, 0, 32, 32);
        bBackward.addActionListener((l) -> {
            SFileNode backNode = Controller.backward();
            if (backNode != null) {
                update(backNode);
            }
        });
        pFunc.add(bBackward);
        // 前进按钮
        bForward = new JButton(getIcon("forward"));
        bForward.setBounds(32, 0, 32, 32);
        bForward.addActionListener((l) -> {
            SFileNode forNode = Controller.forward();
            if (forNode != null) {
                update(forNode);
            }
        });
        pFunc.add(bForward);
        // 上级按钮
        bParentDirectory = new JButton(getIcon("parentDirectory"));
        bParentDirectory.setBounds(64, 0, 32, 32);
        bParentDirectory.addActionListener((l) -> {
            SFileNode parentNode = (SFileNode) (sTableModel.getsFileNode().getParent());
            if (parentNode == null) {
                return;
            }
            update(parentNode);
        });

        pFunc.add(bParentDirectory);

        // 地址文本域
        tfPath = new JTextField(sTableModel.getsFileNode().getPath(), 30);
        tfPath.setBounds(200, 0, tfWidth, 32);
        bRefresh = new JButton(getIcon("refresh"));
        bRefresh.setBounds(200 + tfWidth, 0, 24, 32);
        bRefresh.addActionListener((l) -> {
            String path = tfPath.getText();
            if (path.equals("此电脑") || path.equals("网络") || path.equals("库")) {
                return;
            }
            if (path.equals("search")) {
//                update(controller.search(sTableModel.getsFileNode().getFile(), tfSearch.getText()) );
                return;
            }
            SFileNode gotoNode = new SFileNode(new File(path));
            update(gotoNode);
        });

        pFunc.add(tfPath);
        pFunc.add(bRefresh);

        // 搜索文本域
        tfSearch = new JTextField("search", 30);
        tfSearch.setBounds(280 + tfWidth, 0, tfWidth, 32);
        tfSearch.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!startSearchInput) {
                    tfSearch.setText("");
                    startSearchInput = true;
                }
            }
        });
        bSearch = new JButton(getIcon("search"));
        bSearch.setBounds(280 + 2 * tfWidth, 0, 32, 32);
        bSearch.addActionListener((l) -> {
            String keyword = tfSearch.getText();
            update(controller.search(sTableModel.getsFileNode().getFile(), keyword));
        });
        pFunc.add(tfSearch);
        pFunc.add(bSearch);


        // 菜单栏
        menuBar = new JMenuBar();
        mnFile = new JMenu("文件");
        mnEdit = new JMenu("编辑");
        menuBar.add(mnFile);
        menuBar.add(mnEdit);

        // 文件菜单
        miOpen = new JMenuItem("打开");
        miOpen.addActionListener(controller.getOpenFileListener());
        mnNew = new JMenu("新建");
        miDir = new JMenuItem("文件夹");
        miDir.addActionListener(controller.getNewDirListener());
        miTxt = new JMenuItem("文本文档");
        miTxt.addActionListener(controller.getNewFileListener(".txt"));
        miDelete = new JMenuItem("删除");
        miDelete.addActionListener(controller.getDeleteFileListener());
        miRename = new JMenuItem("重命名");
        miRename.addActionListener(controller.getRenameFileListener());
        mnNew.add(miDir);
        mnNew.add(miTxt);
        mnFile.add(miOpen);
        mnFile.add(mnNew);
        mnFile.add(miDelete);
        mnFile.add(miRename);

        // 编辑菜单
        miCopy = new JMenuItem("复制");
        miCopy.addActionListener(controller.getCopyFileListener());
        miCut = new JMenuItem("剪切");
        miCut.addActionListener(controller.getCutFileListener());
        miPaste = new JMenuItem("粘贴");
        miPaste.addActionListener(controller.getPasteFileListener());
        mnEdit.add(miCopy);
        mnEdit.add(miPaste);
        mnEdit.add(miCut);

        RCMIOpen = new JMenuItem("打开");
        RCMIOpen.addActionListener(controller.getOpenFileListener());
        RCMNNew = new JMenu("新建");
        RCMIDir = new JMenuItem("文件夹");
        RCMIDir.addActionListener(controller.getNewDirListener());
        RCMITxt = new JMenuItem("文本文档");
        RCMITxt.addActionListener(controller.getNewFileListener(".txt"));
        RCMNNew.add(RCMIDir);
        RCMNNew.add(RCMITxt);
        RCMIDelete = new JMenuItem("删除");
        RCMIDelete.addActionListener(controller.getDeleteFileListener());
        RCMIRename = new JMenuItem("重命名");
        RCMIRename.addActionListener(controller.getRenameFileListener());
        RCMICopy = new JMenuItem("复制");
        RCMICopy.addActionListener(controller.getCopyFileListener());
        RCMICut = new JMenuItem("剪切");
        RCMICut.addActionListener(controller.getCutFileListener());
        RCMIPaste = new JMenuItem("粘贴");
        RCMIPaste.addActionListener(controller.getPasteFileListener());
        RCMIZip = new JMenuItem("压缩");
        RCMIZip.addActionListener(controller.getZipFileListener());
        RCMIUnzip = new JMenuItem("解压缩");
        RCMIUnzip.addActionListener(controller.getUnzipFileListener());
        RCMIEnCrypt = new JMenuItem("加密");
        RCMIEnCrypt.addActionListener(controller.getEncryptFileListener());
        RCMIDecrypt = new JMenuItem("解密");
        RCMIDecrypt.addActionListener(controller.getDecryptFileListener());

        rightClickMenu = new JPopupMenu();
        rightClickMenu.add(RCMIOpen);
        rightClickMenu.add(RCMNNew);
        rightClickMenu.add(RCMICopy);
        rightClickMenu.add(RCMIPaste);
        rightClickMenu.add(RCMICut);
        rightClickMenu.add(RCMIRename);
        rightClickMenu.add(RCMIDelete);
        rightClickMenu.add(RCMIZip);
        rightClickMenu.add(RCMIUnzip);
        rightClickMenu.add(RCMIEnCrypt);
        rightClickMenu.add(RCMIDecrypt);


        JSplitPane mainPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, pFunc, splitPane);
        mainPane.setDividerLocation(33);
        mainPane.setContinuousLayout(true);

        // 添加组件
        add(mainPane, BorderLayout.CENTER);
        add(infoTextArea, BorderLayout.SOUTH);
        setJMenuBar(menuBar);
        setItemState();
        refresh();
        String path = tfPath.getText();
        SFileNode gotoNode = new SFileNode(new File(path));
        update(gotoNode);
    }

    private Icon getIcon(String iconName) {
        return new ImageIcon("resource/icons/" + iconName + ".png");
    }

    private static void setItemState() {
        SFileNode node = filesTable.getModel().getsFileNode();
        int selectedRowsCount = filesTable.getSelectedRowCount();
        switch (node.getPath()) {
            case "::{F02C1A0D-BE21-4350-88B0-7367FC96EF3C}":
            case "::{20D04FE0-3AEA-1069-A2D8-08002B30309D}":
            case "::{031E4825-7B94-4DC3-B131-E946B44C8DD5}":
                miOpen.setEnabled(false);
                mnNew.setEnabled(false);
                miPaste.setEnabled(false);
                miCut.setEnabled(false);
                miCopy.setEnabled(false);
                miRename.setEnabled(false);
                miDelete.setEnabled(false);

                RCMNNew.setEnabled(false);
                RCMIOpen.setEnabled(false);
                RCMIDecrypt.setEnabled(false);
                RCMIEnCrypt.setEnabled(false);
                RCMIUnzip.setEnabled(false);
                RCMIZip.setEnabled(false);
                RCMICopy.setEnabled(false);
                RCMICut.setEnabled(false);
                RCMIDelete.setEnabled(false);
                RCMIPaste.setEnabled(false);
                RCMIRename.setEnabled(false);
                if(selectedRowsCount == 1){
                    miOpen.setEnabled(true);
                    RCMIOpen.setEnabled(true);
                }
                break;
            default:
                switch (selectedRowsCount) {
                    // 没有选中文件的情况
                    case 0:
                        miOpen.setEnabled(false);
                        mnNew.setEnabled(true);
                        miPaste.setEnabled(true);
                        miCut.setEnabled(false);
                        miCopy.setEnabled(false);
                        miRename.setEnabled(false);
                        miDelete.setEnabled(false);

                        RCMNNew.setEnabled(true);
                        RCMIOpen.setEnabled(false);
                        RCMIDecrypt.setEnabled(false);
                        RCMIEnCrypt.setEnabled(false);
                        RCMIUnzip.setEnabled(false);
                        RCMIZip.setEnabled(false);
                        RCMICopy.setEnabled(false);
                        RCMICut.setEnabled(false);
                        RCMIDelete.setEnabled(false);
                        RCMIPaste.setEnabled(true);
                        RCMIRename.setEnabled(false);
                        break;
                    case 1: //选中了一个文件的情况
                        // 默认可以执行的操作：打开，剪切，复制，重命名，删除，压缩
                        miOpen.setEnabled(true);
                        mnNew.setEnabled(false);
                        miPaste.setEnabled(false);
                        miCut.setEnabled(true);
                        miCopy.setEnabled(true);
                        miRename.setEnabled(true);
                        miDelete.setEnabled(true);

                        RCMNNew.setEnabled(false);
                        RCMIOpen.setEnabled(true);
                        RCMIDecrypt.setEnabled(false);
                        RCMIEnCrypt.setEnabled(false);
                        RCMIUnzip.setEnabled(false);
                        RCMIZip.setEnabled(true);
                        RCMICopy.setEnabled(true);
                        RCMICut.setEnabled(true);
                        RCMIDelete.setEnabled(true);
                        RCMIPaste.setEnabled(false);
                        RCMIRename.setEnabled(true);

                        // 根据文件名称判断是否可以进行加密、解密、压缩、解压缩操作
                        File selectedFile = ((SFileNode) filesTable.getValueAt(filesTable.getSelectedRow(), -1)).getFile();
                        String name = selectedFile.getName();
                        if (selectedFile.isFile()) {
                            switch (name.substring(name.lastIndexOf("."))) {
                                // 文件是一个加密文件，可以对它解密
                                case ".enc":
                                    RCMIDecrypt.setEnabled(true);
                                    RCMIEnCrypt.setEnabled(false);
                                    break;
                                    // 文件是一个压缩包，可以对它进行解压缩
                                case ".zip":
                                    RCMIUnzip.setEnabled(true);
                                    break;
                                default:
                                    RCMIEnCrypt.setEnabled(true);
                            }
                        }
                        // 选中了一个文件夹
                        break;

                    default:    // 选中了多个文件
                        miOpen.setEnabled(false);
                        mnNew.setEnabled(false);
                        miPaste.setEnabled(false);
                        miCut.setEnabled(true);
                        miCopy.setEnabled(true);
                        miRename.setEnabled(false);
                        miDelete.setEnabled(true);

                        RCMNNew.setEnabled(false);
                        RCMIOpen.setEnabled(false);
                        RCMIDecrypt.setEnabled(false);
                        RCMIEnCrypt.setEnabled(false);
                        RCMIUnzip.setEnabled(false);
                        RCMIZip.setEnabled(true);
                        RCMICopy.setEnabled(true);
                        RCMICut.setEnabled(true);
                        RCMIDelete.setEnabled(true);
                        RCMIPaste.setEnabled(false);
                        RCMIRename.setEnabled(false);
                }
        }
    }

    private static void setTfPath(String nodePath) {
        //网络节点
        if (nodePath.equals("::{F02C1A0D-BE21-4350-88B0-7367FC96EF3C}")) {
            MainFrame.tfPath.setText("网络");
        }
        //计算机节点
        else if (nodePath.equals("::{20D04FE0-3AEA-1069-A2D8-08002B30309D}")) {
            MainFrame.tfPath.setText("此电脑");
        }
        //库节点
        else if (nodePath.equals("::{031E4825-7B94-4DC3-B131-E946B44C8DD5}")) {
            MainFrame.tfPath.setText("库");
        }
        //其他节点
        else {
            MainFrame.tfPath.setText(nodePath);
        }
    }

    private static void setInfoTextArea(String info) {
        infoTextArea.setText(info);
    }

    public static void showFilesPaneRightClickMenu(int x, int y) {
        MainFrame.rightClickMenu.show(MainFrame.filesTable, x, y);
        MainFrame.rightClickMenu.setVisible(true);
    }

    public static void showContentPaneRightClickMenu(int x, int y) {
        MainFrame.rightClickMenu.show(MainFrame.contentTree, x, y);
        MainFrame.rightClickMenu.setVisible(true);
    }

    public static void refresh() {
        update(new SFileNode(filesTable.getModel().getsFileNode().getFile()));
        filesTable.getSelectionModel().clearSelection();
    }

    public static void update(SFileNode node) {
        filesTable.setTable(node, useFileHiding);
        filesTable.getSelectionModel().clearSelection();

        MainFrame.setTfPath(node.getPath());
        Controller.addRecord(node);
        MainFrame.setInfoTextArea("");
        MainFrame.setItemState();
    }

    public static void openFile() {
        int index = filesTable.getSelectedRow();
        if (index < 0) {
            return;
        }
        SFileNode node = (SFileNode) (filesTable.getValueAt(index, -1));
        File file = node.getFile();
        //如果是文件就直接打开
        if (file.isFile()) {
            Runtime run = Runtime.getRuntime();
            try {
                Process process = run.exec("cmd /c call " + "\""
                        + file.getPath() + "\"");
            } catch (IOException e1) {
                e1.printStackTrace();
                System.exit(0);
            }
        } else {
            filesTable.getSelectionModel().clearSelection();
            update(node);  //显示该节点下的文件节点
        }
    }
}

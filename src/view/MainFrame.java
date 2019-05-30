package view;

import controller.Controller;
import controller.TableMouseClick;
import controller.TreeMouseClick;
import model.*;

import javax.swing.*;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.geom.Dimension2D;
import java.io.File;
import java.util.Stack;

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

    private Stack<SFileNode> recordStack;

    private Icon iconBackward = new ImageIcon("resource/icons/backward.png");

    public static JTree contentTree;                // 文件目录树
    public static STable filesTable;                // 文件表
    private STableModel sTableModel;                // 文件表模式
    private JTextArea infoTextArea;                 // 信息文本
    private ListSelectionModel filesTableSelModel;  // 文件表选中模式
    private JPanel pFunc;
    private JButton bParentDirectory;               // 返回上级目录的按钮
    private JButton bBackward;                        // 返回按钮
    private JButton bForward;                       // 前进按钮
    public static JTextField tfPath;                       // 地址框
    private JButton bRefresh;                       // 刷新按钮
    private JTextField tfSearch;                     // 搜索框
    private JButton bSearch;                        // 搜索按钮


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
        // 添加目录点击事件响应
        contentTree.addMouseListener(new TreeMouseClick());

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
        filesTable.addMouseListener(new TableMouseClick());

        filesTableSelModel = filesTable.getSelectionModel();
        filesTableSelModel.addListSelectionListener((e) -> {
            infoTextArea.setText("共" + sTableModel.getsFileNode().getFilesCount(useFileHiding)
                    + "个项目，选中" + getSelectedFilesCount() + "个项目。");
        });


        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(contentTree), new JScrollPane(filesTable));
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
        pFunc.add(bBackward);
        // 前进按钮
        bForward = new JButton(getIcon("forward"));
        bForward.setBounds(32, 0, 32,32);
        pFunc.add(bForward);
        // 上级按钮
        bParentDirectory = new JButton(getIcon("parentDirectory"));
        bParentDirectory.setBounds(64, 0, 32, 32);
        bParentDirectory.addActionListener((l)->{
            SFileNode parentNode = (SFileNode) (sTableModel.getsFileNode().getParent());
            if(parentNode == null){
                return;
            }
            filesTable.setTable(parentNode, useFileHiding);
        });

        pFunc.add(bParentDirectory);

        // 地址文本域
        tfPath = new JTextField(sTableModel.getsFileNode().getPath(), 30);
        tfPath.setBounds(200, 0, tfWidth, 32);
        bRefresh = new JButton(getIcon("refresh"));
        bRefresh.setBounds(200+tfWidth, 0, 32, 32);
        bRefresh.addActionListener((l)->{
            SFileNode gotoNode = new SFileNode(new File(tfPath.getText()));
            filesTable.setTable(gotoNode, useFileHiding);
        });

        pFunc.add(tfPath);
        pFunc.add(bRefresh);

        // 搜索文本域
        tfSearch = new JTextField("search", 30);
        tfSearch.setBounds(280+tfWidth, 0, tfWidth, 32);
        bSearch = new JButton(getIcon("search"));
        bSearch.setBounds(280+2*tfWidth, 0, 32, 32);
        bSearch.addActionListener((l)->{
            String keyword = tfSearch.getText();
            controller.search(keyword);
        });
        pFunc.add(tfSearch);
        pFunc.add(bSearch);
        // 历史记录按钮

        JSplitPane mainPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, pFunc, splitPane);
        mainPane.setDividerLocation(33);
        mainPane.setContinuousLayout(true);

        // 添加组件
        add(mainPane, BorderLayout.CENTER);
        add(infoTextArea, BorderLayout.SOUTH);
    }

    private int getSelectedFilesCount() {
        int count = 0;
        for (int i = filesTableSelModel.getMinSelectionIndex(); i <= filesTableSelModel.getMaxSelectionIndex(); i++) {
            if (filesTableSelModel.isSelectedIndex(i)) {
                count++;
            }
        }
        return count;
    }

    private Icon getIcon(String iconName){
        return new ImageIcon("resource/icons/" + iconName + ".png");
    }

    public static void setTfPath(String nodePath){
        //网络节点
        if (nodePath.equals("::{F02C1A0D-BE21-4350-88B0-7367FC96EF3C}")) {
            MainFrame.tfPath.setText("网络");
        }
        //计算机节点
        else if (nodePath.equals("::{20D04FE0-3AEA-1069-A2D8-08002B30309D}")) {
            MainFrame.tfPath.setText("此电脑");
        }
        //库节点
        else if(nodePath.equals("::{031E4825-7B94-4DC3-B131-E946B44C8DD5}")) {
            MainFrame.tfPath.setText("库");
        }
        //其他节点
        else {
            MainFrame.tfPath.setText(nodePath);
        }
    }
}

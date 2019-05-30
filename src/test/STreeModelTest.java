package test;

import model.SFileNode;
import model.STreeModel;

import java.io.File;

/**
 * Author: Sean
 * Date: Created In 13:04 2019/5/30
 * Title:
 * Description:
 * Version: 0.1
 * Update History:
 * [Date][Version][Author] What has been done;
 */

public class STreeModelTest {
    public static void main(String[] args){
        SFileNode root = new SFileNode( new File("E:\\Download"));
        STreeModel sTreeModel = new STreeModel(root);
    }
}

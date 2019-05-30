package model;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * Author: Sean
 * Date: Created In 18:22 2019/5/30
 * Title:
 * Description:
 * Version: 0.1
 * Update History:
 * [Date][Version][Author] What has been done;
 */

public class STableRenderer extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if(value instanceof JLabel) {
            JLabel label = (JLabel)value;
            label.setOpaque(true);
            if(isSelected){
                label.setBackground(table.getSelectionBackground());
                label.setForeground(table.getSelectionForeground());
            }

            else{
                label.setBackground(table.getBackground());
                label.setForeground(table.getForeground());
            }
            return label;
        }
        return this;
    }
}

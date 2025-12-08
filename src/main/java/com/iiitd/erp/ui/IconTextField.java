package com.iiitd.erp.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class IconTextField extends JTextField {

    private Icon icon;

    public void setIcon(Icon icon) {
        this.icon = icon;
        setBorder(new EmptyBorder(0, icon.getIconWidth() + 5, 0, 0));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (icon != null) {
            int y = (getHeight() - icon.getIconHeight()) / 2;
            icon.paintIcon(this, g, 5, y);
        }
    }
}

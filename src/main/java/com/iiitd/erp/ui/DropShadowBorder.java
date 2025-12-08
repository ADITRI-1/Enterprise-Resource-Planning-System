package com.iiitd.erp.ui;

import javax.swing.border.Border;
import java.awt.*;

public class DropShadowBorder implements Border {
    private int shadowSize = 5;
    private Color shadowColor = Color.BLACK;
    private float shadowOpacity = 0.5f;
    private int cornerSize = 12;

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int shadowX = x + shadowSize;
        int shadowY = y + shadowSize;

        // Paint shadow
        g2d.setColor(shadowColor);
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, shadowOpacity));
        g2d.fillRoundRect(shadowX, shadowY, width - shadowSize, height - shadowSize, cornerSize, cornerSize);

        g2d.dispose();
    }

    @Override
    public Insets getBorderInsets(Component c) {
        return new Insets(0, 0, shadowSize, shadowSize);
    }

    @Override
    public boolean isBorderOpaque() {
        return false;
    }
}

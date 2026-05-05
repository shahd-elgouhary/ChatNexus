package com.chatroom.others;

import java.awt.Color;
import java.awt.Component;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.geom.Area;
import java.awt.geom.RoundRectangle2D;

import javax.swing.border.AbstractBorder;

import com.chatroom.configuration.Config;

public class TextBubbleBorder extends AbstractBorder {

    private boolean isCurrentUser;
    private int radii = 20;
    private int pointerSize = 8;
    private Insets insets;
    private int pointerPad = 15;
    private boolean left;
    private RenderingHints hints;

    public TextBubbleBorder(boolean isCurrentUser) {
        this.isCurrentUser = isCurrentUser;
        this.left = !isCurrentUser;

        hints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int padY = 12;
        int padX = 18;
        
        int leftPad = left ? padX + pointerSize : padX;
        int rightPad = !left ? padX + pointerSize : padX;
        
        insets = new Insets(padY, leftPad, padY, rightPad);
    }

    @Override
    public Insets getBorderInsets(Component c) {
        return insets;
    }

    @Override
    public Insets getBorderInsets(Component c, Insets insets) {
        return getBorderInsets(c);
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        Graphics2D g2 = (Graphics2D) g.create();
        
        RoundRectangle2D.Double bubble = new RoundRectangle2D.Double(
                (left ? pointerSize : 0),
                0,
                width - pointerSize,
                height,
                radii,
                radii);

        Polygon pointer = new Polygon();

        if (left) {
            pointer.addPoint(pointerSize, height - pointerPad - pointerSize);
            pointer.addPoint(pointerSize, height - pointerPad);
            pointer.addPoint(0, height - pointerPad);
        } else {
            pointer.addPoint(width - pointerSize, height - pointerPad - pointerSize);
            pointer.addPoint(width - pointerSize, height - pointerPad);
            pointer.addPoint(width, height - pointerPad);
        }

        Area area = new Area(bubble);
        area.add(new Area(pointer));

        g2.setRenderingHints(hints);
        
        if (isCurrentUser) {
            GradientPaint gp = new GradientPaint(0, 0, Config.getPrimaryGradientStart(), width, height, Config.getPrimaryGradientEnd());
            g2.setPaint(gp);
        } else {
            g2.setColor(Config.isDarkMode ? Color.decode("#334155") : Color.decode("#E2E8F0"));
        }
        
        g2.fill(area);
        
        // Subtle drop shadow/border effect for light mode others
        if (!isCurrentUser && !Config.isDarkMode) {
            g2.setColor(new Color(0, 0, 0, 15));
            g2.draw(area);
        }
        
        g2.dispose();
    }
}
package com.chatroom.ui.icons;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.RoundRectangle2D;

import javax.swing.Icon;

import com.chatroom.configuration.Config;

public class IconFactory {

    private static abstract class VectorIcon implements Icon {
        protected int width;
        protected int height;

        public VectorIcon(int width, int height) {
            this.width = width;
            this.height = height;
        }

        @Override
        public int getIconWidth() { return width; }

        @Override
        public int getIconHeight() { return height; }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.translate(x, y);
            paint(c, g2);
            g2.dispose();
        }

        protected abstract void paint(Component c, Graphics2D g2);
    }

    public static Icon getLogoIcon(int size) {
        return new VectorIcon(size, size) {
            @Override
            protected void paint(Component c, Graphics2D g2) {
                GradientPaint gp = new GradientPaint(0, 0, Config.getPrimaryGradientStart(), size, size, Config.getPrimaryGradientEnd());
                g2.setPaint(gp);
                
                RoundRectangle2D bubble = new RoundRectangle2D.Double(size*0.1, size*0.1, size*0.8, size*0.65, size*0.3, size*0.3);
                g2.fill(bubble);
                
                GeneralPath tail = new GeneralPath();
                tail.moveTo(size*0.3, size*0.7);
                tail.lineTo(size*0.2, size*0.9);
                tail.lineTo(size*0.45, size*0.75);
                tail.closePath();
                g2.fill(tail);
                
                g2.setColor(Color.WHITE);
                g2.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, (int)(size*0.4)));
                java.awt.FontMetrics fm = g2.getFontMetrics();
                int textX = (size - fm.stringWidth("N")) / 2;
                int textY = (int)(size*0.5 + fm.getAscent()/2 - size*0.05);
                g2.drawString("N", textX, textY);
            }
        };
    }

    public static Icon getThemeToggleIcon(int size) {
        return new VectorIcon(size, size) {
            @Override
            protected void paint(Component c, Graphics2D g2) {
                g2.setColor(Config.getTextPrimary());
                if (Config.isDarkMode) {
                    GeneralPath moon = new GeneralPath();
                    moon.append(new Ellipse2D.Double(size*0.2, size*0.2, size*0.6, size*0.6), false);
                    Area moonArea = new Area(moon);
                    Area shadow = new Area(new Ellipse2D.Double(size*0.35, size*0.15, size*0.6, size*0.6));
                    moonArea.subtract(shadow);
                    g2.fill(moonArea);
                } else {
                    g2.fill(new Ellipse2D.Double(size*0.3, size*0.3, size*0.4, size*0.4));
                    g2.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    for (int i=0; i<8; i++) {
                        double angle = i * Math.PI / 4;
                        int x1 = (int)(size/2 + Math.cos(angle) * size*0.25);
                        int y1 = (int)(size/2 + Math.sin(angle) * size*0.25);
                        int x2 = (int)(size/2 + Math.cos(angle) * size*0.4);
                        int y2 = (int)(size/2 + Math.sin(angle) * size*0.4);
                        g2.drawLine(x1, y1, x2, y2);
                    }
                }
            }
        };
    }

    public static Icon getUsersIcon(int size) {
        return new VectorIcon(size, size) {
            @Override
            protected void paint(Component c, Graphics2D g2) {
                g2.setColor(Config.getTextSecondary());
                g2.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                
                // Person 1
                g2.draw(new Ellipse2D.Double(size*0.2, size*0.2, size*0.3, size*0.3));
                GeneralPath p1 = new GeneralPath();
                p1.moveTo(size*0.1, size*0.8);
                p1.quadTo(size*0.35, size*0.5, size*0.6, size*0.8);
                g2.draw(p1);

                // Person 2
                g2.draw(new Ellipse2D.Double(size*0.55, size*0.3, size*0.25, size*0.25));
                GeneralPath p2 = new GeneralPath();
                p2.moveTo(size*0.5, size*0.8);
                p2.quadTo(size*0.65, size*0.6, size*0.9, size*0.8);
                g2.draw(p2);
            }
        };
    }

    public static Icon getExitIcon(int size) {
        return new VectorIcon(size, size) {
            @Override
            protected void paint(Component c, Graphics2D g2) {
                g2.setColor(Config.getTextSecondary());
                g2.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                
                g2.drawRect((int)(size*0.2), (int)(size*0.15), (int)(size*0.4), (int)(size*0.7));
                g2.drawLine((int)(size*0.4), (int)(size*0.5), (int)(size*0.9), (int)(size*0.5));
                g2.drawLine((int)(size*0.7), (int)(size*0.3), (int)(size*0.9), (int)(size*0.5));
                g2.drawLine((int)(size*0.7), (int)(size*0.7), (int)(size*0.9), (int)(size*0.5));
            }
        };
    }

    public static Icon getLogoutIcon(int size) {
        return new VectorIcon(size, size) {
            @Override
            protected void paint(Component c, Graphics2D g2) {
                g2.setColor(Config.COLOR_ERROR);
                g2.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                
                g2.draw(new Ellipse2D.Double(size*0.2, size*0.2, size*0.6, size*0.6));
                g2.drawLine((int)(size*0.5), (int)(size*0.1), (int)(size*0.5), (int)(size*0.5));
            }
        };
    }

    public static java.awt.Image getLogoImage(int size) {
        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(size, size, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        getLogoIcon(size).paintIcon(null, g2, 0, 0);
        g2.dispose();
        return img;
    }

    public static void applyWindowIcon(javax.swing.JFrame frame) {
        try {
            frame.setIconImage(getLogoImage(64));
        } catch (Exception e) {
            try {
                java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(64, 64, java.awt.image.BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2 = img.createGraphics();
                g2.setColor(Config.getPrimary());
                g2.fillRect(0, 0, 64, 64);
                g2.setColor(Color.WHITE);
                g2.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 32));
                g2.drawString("N", 20, 45);
                g2.dispose();
                frame.setIconImage(img);
            } catch (Exception ex) {}
        }
    }
}

package com.chatroom.ui.icons;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import javax.swing.Icon;
import com.chatroom.configuration.Config;

public class AvatarFactory {

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

    public static Icon getAvatarIcon(String username, int size, boolean isOnline) {
        return new VectorIcon(size, size) {
            @Override
            protected void paint(Component c, Graphics2D g2) {
                // Generate deterministic color based on username
                int hash = username.hashCode();
                float hue1 = Math.abs(hash % 360) / 360f;
                float hue2 = (hue1 + 0.15f) % 1.0f;
                Color color1 = Color.getHSBColor(hue1, 0.7f, 0.8f);
                Color color2 = Color.getHSBColor(hue2, 0.8f, 0.9f);

                // Draw gradient circle background
                GradientPaint gp = new GradientPaint(0, 0, color1, size, size, color2);
                g2.setPaint(gp);
                g2.fill(new Ellipse2D.Double(0, 0, size, size));

                // Draw Initials
                String initials = getInitials(username);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, (int)(size * 0.4)));
                FontMetrics fm = g2.getFontMetrics();
                int textX = (size - fm.stringWidth(initials)) / 2;
                int textY = (int)(size * 0.5 + fm.getAscent() / 2 - size * 0.05);
                g2.drawString(initials, textX, textY);

                // Draw Online/Offline Status Indicator
                int dotSize = (int)(size * 0.25);
                int dotX = size - dotSize;
                int dotY = size - dotSize;

                // Draw indicator border
                g2.setColor(Config.getSurface());
                g2.fill(new Ellipse2D.Double(dotX - 1, dotY - 1, dotSize + 2, dotSize + 2));

                // Draw indicator fill
                if (isOnline) {
                    g2.setColor(Config.COLOR_SUCCESS);
                } else {
                    g2.setColor(Config.getTextSecondary());
                }
                g2.fill(new Ellipse2D.Double(dotX, dotY, dotSize, dotSize));
            }
        };
    }

    private static String getInitials(String username) {
        if (username == null || username.trim().isEmpty()) {
            return "?";
        }
        username = username.trim();
        if (username.length() == 1) {
            return username.toUpperCase();
        }
        
        // If there is a space, take first letter of first two words
        String[] parts = username.split("\\s+");
        if (parts.length >= 2) {
            return (parts[0].substring(0, 1) + parts[1].substring(0, 1)).toUpperCase();
        }
        
        // Otherwise take first two letters
        return username.substring(0, 2).toUpperCase();
    }
}

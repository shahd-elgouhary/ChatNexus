package com.chatroom.configuration;

import java.awt.Color;
import java.io.StringWriter;

public class Config {
	public static String USER_NAME = "root";
	public static String USER_PWD = "root";
	public static String DATABASE_HOST = "localhost";
	public static String DATABASE_URL = "jdbc:mysql://";
	public static String DATABASE_PORT = ":3306";
	public static final String DATABASE_NAME = "chatroom";
	public static final String TABLE_NAME = "users";
	public static final String CLIENT_ID = "client_id";
	public static final String CLIENT_NAME = "client_name";
	public static final String CLIENT_PWD = "client_pwd";
	
	public static boolean isDarkMode = false;
	
	public static Color getPrimaryGradientStart() { return isDarkMode ? Color.decode("#6366F1") : Color.decode("#4F46E5"); }
	public static Color getPrimaryGradientEnd() { return isDarkMode ? Color.decode("#A855F7") : Color.decode("#7C3AED"); }
	
	public static Color getPrimary() { return getPrimaryGradientStart(); }
	
	public static Color getBackground() { return isDarkMode ? Color.decode("#0F172A") : Color.decode("#F8FAFC"); }
	public static Color getSurface() { return isDarkMode ? Color.decode("#1E293B") : Color.decode("#FFFFFF"); }
	public static Color getBorder() { return isDarkMode ? Color.decode("#334155") : Color.decode("#E2E8F0"); }
	
	public static Color getTextPrimary() { return isDarkMode ? Color.decode("#F1F5F9") : Color.decode("#0F172A"); }
	public static Color getTextSecondary() { return isDarkMode ? Color.decode("#94A3B8") : Color.decode("#64748B"); }

	public static final Color COLOR_SUCCESS = Color.decode("#10B981");
	public static final Color COLOR_ERROR = Color.decode("#EF4444");
	public static final Color COLOR_WARNING = Color.decode("#F97316");
	public static final Color COLOR_ONLINE = Color.decode("#22C55E");
	
	public static StringWriter errors = new StringWriter();
}

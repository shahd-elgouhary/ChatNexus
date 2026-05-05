package com.chatroom.client;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import com.chatroom.configuration.Config;
import com.chatroom.others.LogFileWriter;
import com.chatroom.ui.MainSplash;

import javax.swing.*;
import com.formdev.flatlaf.FlatLightLaf;

public class ClientExec {
	public static void main(String[] args) {		
		try {
			UIManager.setLookAndFeel(new FlatLightLaf());
			UIManager.put("Button.arc", 15);
			UIManager.put("Component.arc", 15);
			UIManager.put("TextComponent.arc", 15);
		} catch (Exception ex) {
			System.err.println("Failed to initialize FlatLaf");
		}
		//create the log file if it is not present
		String path = System.getProperty("user.home");
		path += "/CHATROOM";
		File file = new File(path);
		if(!file.exists())
			file.mkdir();
				
		File f = new File(System.getProperty("user.home")+"/CHATROOM/LOGS.txt");
		if(!f.exists()) {
			try {
				f.createNewFile();
			} catch (IOException e) {
				e.printStackTrace(new PrintWriter(Config.errors));
				LogFileWriter.Log(Config.errors.toString());
			}
		}
		
		if(args.length == 3 && args[2].equals("--console")) {
			Client client = new Client(args[0],Integer.parseInt(args[1]));
			client.connect();
		}
		else {
			final String host = (args.length >= 2) ? args[0] : "127.0.0.1";
			final int port = (args.length >= 2) ? Integer.parseInt(args[1]) : 1234;
			
			// Show a loading splash immediately on EDT
			JFrame loadingFrame = new JFrame("ChatNexus");
			JLabel loadingLabel = new JLabel("Connecting to server...", SwingConstants.CENTER);
			loadingLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 16));
			loadingLabel.setForeground(Config.getTextSecondary());
			loadingFrame.setContentPane(new JPanel(new java.awt.BorderLayout()));
			loadingFrame.getContentPane().setBackground(Config.getBackground());
			loadingFrame.getContentPane().add(loadingLabel, java.awt.BorderLayout.CENTER);
			loadingFrame.setSize(400, 200);
			loadingFrame.setLocationRelativeTo(null);
			loadingFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			loadingFrame.setResizable(false);
			loadingFrame.setVisible(true);
			
			// Connect on a background thread to avoid blocking the EDT
			new Thread(() -> {
				ClientModel cm = new ClientModel(host, port);
				SwingUtilities.invokeLater(() -> {
					loadingFrame.dispose();
					if (cm.isConnected()) {
						try {
							new MainSplash(cm);
						} catch (IOException ex) {
							ex.printStackTrace();
							JOptionPane.showMessageDialog(null, 
								"Failed to start the application: " + ex.getMessage(),
								"Error", JOptionPane.ERROR_MESSAGE);
							System.exit(1);
						}
					} else {
						JOptionPane.showMessageDialog(null,
							"Could not connect to server at " + host + ":" + port + ".\n\n" +
							"Make sure the server is running:\n" +
							"  java -jar server.jar " + port + " " + host + " root root",
							"Connection Failed", JOptionPane.ERROR_MESSAGE);
						System.exit(1);
					}
				});
			}, "Connection-Thread").start();
		}
	}
}

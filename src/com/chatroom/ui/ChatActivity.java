package com.chatroom.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;

import com.chatroom.client.ClientModel;
import com.chatroom.configuration.Config;
import com.chatroom.models.Request;
import com.chatroom.models.Response;
import com.chatroom.others.LogFileWriter;
import com.chatroom.others.TextBubbleBorder;
import com.chatroom.ui.icons.IconFactory;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;

public class ChatActivity {
	private JFrame jFrame;
	private JButton jBtnSend;
	private JTextField jTfMessageHere;
	private JPanel jPanelBottom;
	private JPanel jPanelChatWindow;
	private JPanel optionsButtonsHolder;
	private int i = 0;
	private JScrollPane jScrollPane;
	private AbstractBorder leftBubble;
	private AbstractBorder rightBubble;
	private GridBagConstraints leftBubbleConstraints;
	private GridBagConstraints rightBubbleConstraints;
	private GridBagConstraints centerConstraints;
	private ClientModel clientModel;
	private Request request = null;
	private Response response = null;
	private MessageListener messageListener;
	private JButton jLabel_logout;
	private JButton jLabel_exit;
	private JButton jLabel_live;
	private JButton jLabel_theme;
	private JLabel roomLabel;
	private JPanel headerPanel;
	private JPanel leftHeader;
	private int tracker; 
	private boolean isSenderMsg;
	private boolean isReadMode;
	private int scrollDistance = 300;
	private JLabel jLabelTyping;
	private javax.swing.Timer typingHideTimer;
	private boolean isSendingTypingEvent = false;

	public ChatActivity(ClientModel clientModel) throws IOException {
		this.clientModel = clientModel;
		jFrame = new JFrame("ChatNexus - Room " + clientModel.getRoomId());
		com.chatroom.ui.icons.IconFactory.applyWindowIcon(jFrame);
		jPanelBottom = new JPanel(new BorderLayout(10, 10));
		jPanelChatWindow = new JPanel(new GridBagLayout());
		optionsButtonsHolder = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));

		messageListener = new MessageListener();
		messageListener.start();

		leftBubble = new TextBubbleBorder(false);
		rightBubble = new TextBubbleBorder(true);

		jLabel_exit = new JButton(IconFactory.getExitIcon(24));
		jLabel_live = new JButton(IconFactory.getUsersIcon(24));
		jLabel_logout = new JButton(IconFactory.getLogoutIcon(24));
		jLabel_theme = new JButton(IconFactory.getThemeToggleIcon(24));
		
		jLabel_exit.setContentAreaFilled(false);
		jLabel_live.setContentAreaFilled(false);
		jLabel_logout.setContentAreaFilled(false);
		jLabel_theme.setContentAreaFilled(false);
		
		jLabel_exit.setBorderPainted(false);
		jLabel_live.setBorderPainted(false);
		jLabel_logout.setBorderPainted(false);
		jLabel_theme.setBorderPainted(false);
		
		jLabel_exit.setFocusPainted(false);
		jLabel_live.setFocusPainted(false);
		jLabel_logout.setFocusPainted(false);
		jLabel_theme.setFocusPainted(false);

		jLabel_exit.setToolTipText("Leave Room");
		jLabel_live.setToolTipText("Online Users");
		jLabel_logout.setToolTipText("Logout");
		jLabel_theme.setToolTipText("Toggle Theme");

		jLabel_exit.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
		jLabel_live.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
		jLabel_logout.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
		jLabel_theme.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

		optionsButtonsHolder.setBackground(Config.getSurface());
		optionsButtonsHolder.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Config.getBorder()));
		
		roomLabel = new JLabel("Room " + clientModel.getRoomId());
		roomLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
		roomLabel.setForeground(Config.getTextPrimary());
		
		leftHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
		leftHeader.setBackground(Config.getSurface());
		leftHeader.add(roomLabel);

		headerPanel = new JPanel(new BorderLayout());
		headerPanel.add(leftHeader, BorderLayout.WEST);
		
		optionsButtonsHolder.add(jLabel_theme);
		optionsButtonsHolder.add(jLabel_live);
		optionsButtonsHolder.add(jLabel_exit);
		optionsButtonsHolder.add(jLabel_logout);
		headerPanel.add(optionsButtonsHolder, BorderLayout.EAST);

		rightBubbleConstraints = new GridBagConstraints(0, i, 1, 1, 1.0, 0, GridBagConstraints.NORTHEAST, GridBagConstraints.NONE, new Insets(5, 50, 5, 10), 0, 0);
		leftBubbleConstraints = new GridBagConstraints(0, i, 1, 1, 1.0, 0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(5, 10, 5, 50), 0, 0);
		centerConstraints = new GridBagConstraints(0, i, 1, 1, 1.0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5, 0, 5, 0), 0, 0);

		jTfMessageHere = new JTextField();
		jBtnSend = new JButton("Send");

		initializeAllWithProperties();
		displayStatusMessages("You joined the room");

		request = new Request(Request.Type.STATUS_MSG.ordinal(), clientModel.getClientID(), clientModel.getRoomId(), "joined the chat");
		try {
			clientModel.getStreamManager().writeObject(request);
		} catch (IOException e) {}
	}

	private void updateTheme() {
		try {
			if (Config.isDarkMode) {
				UIManager.setLookAndFeel(new FlatDarkLaf());
			} else {
				UIManager.setLookAndFeel(new FlatLightLaf());
			}
			UIManager.put("Button.arc", 15);
			UIManager.put("Component.arc", 15);
			UIManager.put("TextComponent.arc", 15);
			SwingUtilities.updateComponentTreeUI(jFrame);
			
			// Refresh custom dynamic colors
			jPanelChatWindow.setBackground(Config.getBackground());
			jPanelBottom.setBackground(Config.getSurface());
			optionsButtonsHolder.setBackground(Config.getSurface());
			leftHeader.setBackground(Config.getSurface());
			roomLabel.setForeground(Config.getTextPrimary());
			optionsButtonsHolder.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Config.getBorder()));
			
			jLabel_theme.setIcon(IconFactory.getThemeToggleIcon(24));
			jLabel_live.setIcon(IconFactory.getUsersIcon(24));
			jLabel_exit.setIcon(IconFactory.getExitIcon(24));
			
			jFrame.repaint();
		} catch (Exception e) {}
	}

	private void updateScrollbarPosition() {
		jScrollPane.getVerticalScrollBar().setValue(jScrollPane.getVerticalScrollBar().getMaximum());
	}

	private void ListeningEvents() {
		jLabel_theme.addActionListener(e -> {
			Config.isDarkMode = !Config.isDarkMode;
			updateTheme();
		});

		jBtnSend.addActionListener(e -> {
			tracker = jScrollPane.getVerticalScrollBar().getMaximum();
			setSenderMessage();
			isSenderMsg = true;
		});

		jLabel_logout.addActionListener(e -> {
			System.out.println("[DEBUG] BUTTON CLICKED: Logout");
			new Thread(() -> {
				try {
					Request req = new Request(Request.Type.MSG.ordinal(), clientModel.getClientID(), clientModel.getRoomId(), "sv_logout");
					clientModel.getStreamManager().writeObject(req);
				} catch (IOException e1) {}
			}).start();
		});

		jLabel_exit.addActionListener(e -> {
			System.out.println("[DEBUG] BUTTON CLICKED: Exit Room");
			new Thread(() -> {
				try {
					Request req = new Request(Request.Type.MSG.ordinal(), clientModel.getClientID(), clientModel.getRoomId(), "sv_exit");
					clientModel.getStreamManager().writeObject(req);
				} catch (IOException e1) {}
			}).start();
		});

		jLabel_live.addActionListener(e -> {
			System.out.println("[DEBUG] BUTTON CLICKED: Show Online Users");
			new Thread(() -> {
				try {
					Request req = new Request(Request.Type.MSG.ordinal(), clientModel.getClientID(), clientModel.getRoomId(), "sv_showusers");
					clientModel.getStreamManager().writeObject(req);
				} catch (IOException e1) {}
			}).start();
		});

		jScrollPane.getVerticalScrollBar().addAdjustmentListener(e -> {
			JScrollBar jsb = (JScrollBar) e.getAdjustable();
			int e1 = jsb.getModel().getExtent();

			if ((jsb.getValue() + e1) <= jsb.getMaximum() - scrollDistance) {
				isReadMode = true;
				scrollDistance = 10;
			} else {
				isReadMode = false;
				scrollDistance = 300;
			}

			if (isSenderMsg || (!isReadMode && tracker != jsb.getMaximum())) {
				updateScrollbarPosition();
				tracker = jsb.getMaximum();
			}
			if (isSenderMsg) isSenderMsg = false;
		});

		jTfMessageHere.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "enter");
		jTfMessageHere.getActionMap().put("enter", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				tracker = jScrollPane.getVerticalScrollBar().getMaximum();
				setSenderMessage();
				isSenderMsg = true;
			}
		});

		jTfMessageHere.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
			@Override
			public void insertUpdate(javax.swing.event.DocumentEvent e) { sendTypingEvent(); }
			@Override
			public void removeUpdate(javax.swing.event.DocumentEvent e) { sendTypingEvent(); }
			@Override
			public void changedUpdate(javax.swing.event.DocumentEvent e) { sendTypingEvent(); }
			
			private void sendTypingEvent() {
				if (!isSendingTypingEvent && jTfMessageHere.getText().length() > 0) {
					isSendingTypingEvent = true;
					try {
						request = new Request(Request.Type.MSG.ordinal(), clientModel.getClientID(), clientModel.getRoomId(), "sv_typing");
						clientModel.getStreamManager().writeObject(request);
					} catch (IOException ex) {}
					
					javax.swing.Timer t = new javax.swing.Timer(2000, evt -> isSendingTypingEvent = false);
					t.setRepeats(false);
					t.start();
				}
			}
		});
	}

	private String getTime() {
		return new SimpleDateFormat("HH:mm").format(new Date());
	}

	private void setSenderMessage() {
		String msg = jTfMessageHere.getText().trim();
		if (!msg.isEmpty() && msg.length() <= 300) {
			try {
				request = new Request(Request.Type.MSG.ordinal(), clientModel.getClientID(), clientModel.getRoomId(), msg);
				clientModel.getStreamManager().writeObject(request);

				// Build message panel with text and timestamp
				JPanel bubblePanel = new JPanel();
				bubblePanel.setLayout(new java.awt.BorderLayout(0, 4));
				bubblePanel.setOpaque(false);
				bubblePanel.setBorder(rightBubble);

				JLabel msgLabel = new JLabel("<html><div style='width:200px;'>" + msg + "</div></html>");
				msgLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
				msgLabel.setForeground(Color.WHITE);
				bubblePanel.add(msgLabel, java.awt.BorderLayout.CENTER);

				JLabel timeLabel = new JLabel(getTime());
				timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
				timeLabel.setForeground(new Color(200, 200, 200));
				timeLabel.setHorizontalAlignment(SwingConstants.RIGHT);
				bubblePanel.add(timeLabel, java.awt.BorderLayout.SOUTH);

				javax.swing.JPanel wrapper = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 10, 0));
				wrapper.setOpaque(false);
				wrapper.add(bubblePanel);
				wrapper.add(new JLabel(com.chatroom.ui.icons.AvatarFactory.getAvatarIcon("Me", 36, true)));

				rightBubbleConstraints.gridy = i++;
				jPanelChatWindow.add(wrapper, rightBubbleConstraints);
				jPanelChatWindow.revalidate();
				jPanelChatWindow.repaint();
				jTfMessageHere.setText("");
				jTfMessageHere.requestFocus();
			} catch (IOException e) {}
		} else {
			JOptionPane.showMessageDialog(jFrame, "Message length must be between 1 and 300 characters.", "Invalid Message", JOptionPane.WARNING_MESSAGE);
		}
	}

	private void displayStatusMessages(String message) {
		JLabel jLabelMessage = new JLabel();
		String bgHex = Config.isDarkMode ? "#334155" : "#E5E7EB";
		String textHex = Config.isDarkMode ? "#94A3B8" : "#6B7280";
		String text = String.format("<html><div style='text-align:center; color: %s; font-family: Segoe UI; font-size: 12px; padding: 6px 12px; background-color: %s; border-radius: 12px;'>%s</div></html>", textHex, bgHex, message);
		jLabelMessage.setText(text);
		centerConstraints.gridy = i++;
		jPanelChatWindow.add(jLabelMessage, centerConstraints);
		jPanelChatWindow.revalidate();
		jPanelChatWindow.repaint();
	}

	private void setReceiverMessage(String senderName, String message) {
		// Build message panel with sender name, text, and timestamp
		JPanel bubblePanel = new JPanel();
		bubblePanel.setLayout(new java.awt.BorderLayout(0, 4));
		bubblePanel.setOpaque(false);
		bubblePanel.setBorder(leftBubble);

		JLabel nameLabel = new JLabel(senderName);
		nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
		nameLabel.setForeground(Config.isDarkMode ? new Color(168, 85, 247) : new Color(79, 70, 229));
		bubblePanel.add(nameLabel, java.awt.BorderLayout.NORTH);

		JLabel msgLabel = new JLabel("<html><div style='width:200px;'>" + message + "</div></html>");
		msgLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		msgLabel.setForeground(Config.isDarkMode ? new Color(241, 245, 249) : new Color(17, 24, 39));
		bubblePanel.add(msgLabel, java.awt.BorderLayout.CENTER);

		JLabel timeLabel = new JLabel(getTime());
		timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
		timeLabel.setForeground(Config.isDarkMode ? new Color(148, 163, 184) : new Color(156, 163, 175));
		timeLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		bubblePanel.add(timeLabel, java.awt.BorderLayout.SOUTH);

		javax.swing.JPanel wrapper = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 10, 0));
		wrapper.setOpaque(false);
		wrapper.add(new JLabel(com.chatroom.ui.icons.AvatarFactory.getAvatarIcon(senderName, 36, true)));
		wrapper.add(bubblePanel);

		leftBubbleConstraints.gridy = i++;
		jPanelChatWindow.add(wrapper, leftBubbleConstraints);
		jPanelChatWindow.revalidate();
		jPanelChatWindow.repaint();
	}

	private void initializeAllWithProperties() {
		jTfMessageHere.setPreferredSize(new Dimension(650, 45));
		jTfMessageHere.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		jTfMessageHere.putClientProperty("JTextField.placeholderText", "Type a message...");

		jBtnSend.setPreferredSize(new Dimension(100, 45));
		jBtnSend.setBackground(Config.getPrimary());
		jBtnSend.setForeground(Color.WHITE);
		jBtnSend.setFont(new Font("Segoe UI", Font.BOLD, 14));
		jBtnSend.setFocusPainted(false);
		jBtnSend.putClientProperty("JButton.buttonType", "roundRect");

		jPanelBottom.setBorder(new EmptyBorder(10, 15, 10, 15));
		jPanelBottom.setBackground(Config.getSurface());
		jPanelBottom.add(jTfMessageHere, BorderLayout.CENTER);
		jPanelBottom.add(jBtnSend, BorderLayout.EAST);
		
		jLabelTyping = new JLabel(" ");
		jLabelTyping.setFont(new Font("Segoe UI", Font.ITALIC, 11));
		jLabelTyping.setForeground(Config.getTextSecondary());
		jPanelBottom.add(jLabelTyping, BorderLayout.NORTH);

		typingHideTimer = new javax.swing.Timer(2500, e -> {
			jLabelTyping.setText(" ");
			jPanelBottom.revalidate();
			jPanelBottom.repaint();
		});
		typingHideTimer.setRepeats(false);

		jPanelChatWindow.setBackground(Config.getBackground());

		jScrollPane = new JScrollPane(jPanelChatWindow);
		jScrollPane.setBorder(BorderFactory.createEmptyBorder());
		jScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		jScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		jScrollPane.getVerticalScrollBar().setUnitIncrement(16);

		jFrame.setLayout(new BorderLayout());
		jFrame.add(headerPanel, BorderLayout.NORTH);
		jFrame.add(jScrollPane, BorderLayout.CENTER);
		jFrame.add(jPanelBottom, BorderLayout.SOUTH);

		jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jFrame.setSize(864, 614);
		jFrame.setLocationRelativeTo(null);
		jFrame.setVisible(true);
		jFrame.setResizable(true);
		jFrame.setMinimumSize(new Dimension(800, 600));

		jFrame.getRootPane().setDefaultButton(jBtnSend);
		jTfMessageHere.requestFocus();

		ListeningEvents();
	}

	class MessageListener extends Thread {
		volatile boolean isContinue = true;

		public void run() {
			while (isContinue) {
				try {
					final Response resp = (Response) clientModel.getStreamManager().readObject();
					SwingUtilities.invokeLater(() -> {
						if (!isContinue) return; // Already exited
						
						if (resp.getId() == Response.Type.STATUS_MSG.ordinal() || resp.getId() == Response.Type.LOGOUT.ordinal()) {
							if (resp.getId() == Response.Type.LOGOUT.ordinal()) {
								clientModel.setRoomId(-1);
								clientModel.setClientID(-1);
								clientModel.getStreamManager().close();
								isContinue = false;
								try {
									new com.chatroom.ui.SignInActivity(new com.chatroom.client.ClientModel(clientModel.getHost(), clientModel.getPort()));
								} catch (IOException ex) {}
								jFrame.dispose();
								return;
							}
							if (resp.getContents().equals("sv_exit_successful")) {
								clientModel.setRoomId(-1);
								isContinue = false;
								try {
									new MainMenuOptions(clientModel);
								} catch (IOException ex) {
									ex.printStackTrace();
								}
								jFrame.dispose();
								return;
							}
							displayStatusMessages(resp.getContents());
						} else if (resp.getId() == Response.Type.GEN.ordinal()) {
							System.out.println("[DEBUG] CLIENT RECEIVED MESSAGE: (Room Members List)");
							String data = "Online Users:\n\n1. You\n";
							String temp = resp.getContents();
							if (temp != null && !temp.isEmpty()) {
								String[] arrayOFNames = temp.split(",");
								int idx = 2;
								for (String name : arrayOFNames) {
									data += idx++ + ". " + name + "\n";
								}
							}
							JOptionPane.showMessageDialog(jFrame, data, "Room Members", JOptionPane.INFORMATION_MESSAGE);
						} else {
							String msg = resp.getContents();
							System.out.println("[DEBUG] CLIENT RECEIVED MESSAGE: " + msg);
							if (msg == null || !msg.contains(" ")) return;
							String name = msg.substring(0, msg.indexOf(" "));
							String content = msg.substring(msg.indexOf(" ") + 1);
							
							// Filter out server command echoes
							if (content.startsWith("sv_typing") || content.startsWith("sv_exit") || content.startsWith("sv_logout") || content.startsWith("sv_showusers")) {
								return;
							}
							
							if (resp.getId() == Response.Type.P_MSG.ordinal()) name += " (Private)";
							setReceiverMessage(name, content);
						}

						if (resp.getId() == Response.Type.LOGOUT.ordinal() && resp.getContents().contains("successfully")) {
							isContinue = false;
							clientModel.setRoomId(-1);
							clientModel.setClientID(-1);
							try {
								new SignInActivity(clientModel);
								jFrame.dispose();
							} catch (IOException e) {}
						}
					});
				} catch (ClassNotFoundException | IOException e) {
					break;
				}
			}
		}
	}
}

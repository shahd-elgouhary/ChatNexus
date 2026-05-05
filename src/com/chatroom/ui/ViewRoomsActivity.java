package com.chatroom.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.PrintWriter;

import javax.imageio.ImageIO;
import javax.swing.*;

import com.chatroom.client.ClientModel;
import com.chatroom.configuration.Config;
import com.chatroom.models.Request;
import com.chatroom.models.Response;
import com.chatroom.others.LogFileWriter;

public class ViewRoomsActivity {
	private JLabel jLabel;
	private JLabel jLabelTitle;
	private JFrame jFrame;
	private JButton jBtnJoinRoom;
	private JComboBox<String> jComboBox;
	private BufferedImage iconLogo;
	private BufferedImage back_arrow_image;
	private JLabel jLabel_back_arrow_image;
	private ClientModel clientModel;
	private Request request = null;
	private Response response = null;

	public ViewRoomsActivity(ClientModel cm) throws IOException {
		clientModel = cm;
		jFrame = new JFrame("ChatNexus - View Rooms");
		com.chatroom.ui.icons.IconFactory.applyWindowIcon(jFrame);

		try {
			iconLogo = ImageIO.read(this.getClass().getResource("/logo.png"));
			back_arrow_image = ImageIO.read(this.getClass().getResource("/back_arrow.png"));
		} catch (Exception e) {}

		jFrame.setContentPane(new JPanel() {
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				g.setColor(Config.getBackground());
				g.fillRect(0, 0, getWidth(), getHeight());
			}
		});

		jBtnJoinRoom = new JButton("JOIN ROOM");
		jLabelTitle = new JLabel("Available Rooms");
		jLabelTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
		jLabelTitle.setForeground(Config.getTextPrimary());

		try {
			jComboBox = new JComboBox<>(getRooms());
			jComboBox.setRenderer(new javax.swing.ListCellRenderer<String>() {
				private javax.swing.JPanel panel = new javax.swing.JPanel(new java.awt.BorderLayout(10, 0));
				private javax.swing.JLabel label = new javax.swing.JLabel();
				private javax.swing.JLabel badge = new javax.swing.JLabel() {
					@Override
					protected void paintComponent(java.awt.Graphics g) {
						java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
						g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
						g2.setColor(Config.COLOR_ERROR);
						g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
						super.paintComponent(g);
						g2.dispose();
					}
				};

				{
					panel.setOpaque(true);
					panel.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 12, 8, 12));
					label.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
					
					badge.setForeground(java.awt.Color.WHITE);
					badge.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 10));
					badge.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
					badge.setPreferredSize(new java.awt.Dimension(22, 22));
					
					panel.add(label, java.awt.BorderLayout.CENTER);
					panel.add(badge, java.awt.BorderLayout.EAST);
				}

				@Override
				public java.awt.Component getListCellRendererComponent(javax.swing.JList<? extends String> list, String value, int index, boolean isSelected, boolean cellHasFocus) {
					label.setText(value);
					
					// Generate deterministic pseudo-random badge count for demo aesthetics
					int unread = (value != null) ? (Math.abs(value.hashCode()) % 5) : 0;
					if (unread > 0) {
						badge.setText(String.valueOf(unread));
						badge.setVisible(true);
					} else {
						badge.setVisible(false);
					}

					if (isSelected) {
						panel.setBackground(Config.getPrimary());
						label.setForeground(java.awt.Color.WHITE);
					} else {
						panel.setBackground(Config.getSurface());
						label.setForeground(Config.getTextPrimary());
					}
					return panel;
				}
			});
			initializeAllWithProperties();
		} catch (RuntimeException e) {
			e.printStackTrace(new PrintWriter(Config.errors));
			LogFileWriter.Log(Config.errors.toString());
			new MainMenuOptions(clientModel);
			jFrame.dispose();
		}
	}

	private String[] getRooms() {
		String[] rooms = {};
		Request request = new Request(Request.Type.VIEW_ROOMS.ordinal(), clientModel.getClientID(), clientModel.getRoomId(), "");
		try {
			clientModel.getStreamManager().writeObject(request);
			Response response = (Response) clientModel.getStreamManager().readObject();
			if (response.getSuccess()) {
				rooms = response.getContents().split("\n");
			} else {
				JOptionPane.showMessageDialog(null, response.getContents(), "Info", JOptionPane.INFORMATION_MESSAGE);
				throw new RuntimeException("no_rooms");
			}
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace(new PrintWriter(Config.errors));
			LogFileWriter.Log(Config.errors.toString());
		}
		return rooms;
	}

	private void createAndJoinRoom(String rName, boolean create) {
		new Thread(() -> {
			try {
				if (create)
					request = new Request(Request.Type.CREATE_ROOM.ordinal(), clientModel.getClientID(), clientModel.getRoomId(), rName);
				else
					request = new Request(Request.Type.JOIN_ROOM.ordinal(), clientModel.getClientID(), clientModel.getRoomId(), rName);

				clientModel.getStreamManager().writeObject(request);
				Object obj = clientModel.getStreamManager().readObject();
				if (obj.getClass() == Response.class)
					response = (Response) obj;
				else {
					throw new Exception("Object returned is not of type Response");
				}
				if (response.getSuccess()) {
					String contents = response.getContents();
					int roomId = -1;
					int hashIndex = contents.indexOf('#');
					if (hashIndex >= 0) {
						int spaceIndex = contents.indexOf(' ', hashIndex);
						if (spaceIndex < 0) spaceIndex = contents.length();
						try {
							roomId = Integer.parseInt(contents.substring(hashIndex + 1, spaceIndex).trim());
						} catch (NumberFormatException nfe) {}
					}
					if (roomId < 0) {
						java.util.regex.Matcher m = java.util.regex.Pattern.compile("\\d+").matcher(contents);
						if (m.find()) roomId = Integer.parseInt(m.group());
					}
					clientModel.setRoomId(roomId);
					SwingUtilities.invokeLater(() -> {
						try {
							new ChatActivity(clientModel);
						} catch (IOException ex) {
							ex.printStackTrace();
						}
						jFrame.dispose();
					});
				} else {
					SwingUtilities.invokeLater(() ->
						JOptionPane.showMessageDialog(null, response.getContents(), "Error", JOptionPane.ERROR_MESSAGE)
					);
				}
			} catch (Exception e) {
				e.printStackTrace(new PrintWriter(Config.errors));
				LogFileWriter.Log(Config.errors.toString());
			}
		}, "ViewRooms-Network").start();
	}

	private void ListeningEvents() {
		jBtnJoinRoom.addActionListener(e -> {
			try {
				if (jComboBox.getSelectedItem() != null) {
					createAndJoinRoom(String.valueOf(jComboBox.getSelectedItem()).trim(), false);
				}
			} catch (Exception e1) {
				e1.printStackTrace(new PrintWriter(Config.errors));
				LogFileWriter.Log(Config.errors.toString());
			}
		});

		jLabel_back_arrow_image.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				try {
					new MainMenuOptions(clientModel);
				} catch (IOException e1) {
					e1.printStackTrace(new PrintWriter(Config.errors));
					LogFileWriter.Log(Config.errors.toString());
				}
				jFrame.dispose();
			};
		});
	}

	private void initializeAllWithProperties() {
		jBtnJoinRoom.setPreferredSize(new Dimension(250, 45));
		jBtnJoinRoom.setBackground(Config.getPrimary());
		jBtnJoinRoom.setForeground(Color.white);
		jBtnJoinRoom.setFont(new Font("Segoe UI", Font.BOLD, 14));
		jBtnJoinRoom.setFocusPainted(false);
		jBtnJoinRoom.putClientProperty("JButton.buttonType", "roundRect");

		jComboBox.setPreferredSize(new Dimension(300, 40));
		jComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));

		jFrame.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.CENTER;

		if (back_arrow_image != null) {
			jLabel_back_arrow_image = new JLabel(new ImageIcon(back_arrow_image.getScaledInstance(32, 32, Image.SCALE_SMOOTH)));
			jLabel_back_arrow_image.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
			
			GridBagConstraints cBack = new GridBagConstraints();
			cBack.anchor = GridBagConstraints.NORTHWEST;
			cBack.insets = new Insets(20, 20, 0, 0);
			cBack.weightx = 1.0;
			cBack.weighty = 1.0;
			cBack.gridx = 0;
			cBack.gridy = 0;
			jFrame.add(jLabel_back_arrow_image, cBack);
		}

		int gridy = 1;
		jLabel = new JLabel(com.chatroom.ui.icons.IconFactory.getLogoIcon(100));
		c.gridy = gridy++;
		c.insets = new Insets(0, 0, 20, 0);
		jFrame.add(jLabel, c);

		c.gridy = gridy++;
		c.insets = new Insets(0, 0, 30, 0);
		jFrame.add(jLabelTitle, c);

		c.gridy = gridy++;
		c.insets = new Insets(0, 0, 15, 0);
		jFrame.add(jComboBox, c);

		c.gridy = gridy++;
		c.insets = new Insets(15, 0, 40, 0);
		jFrame.add(jBtnJoinRoom, c);

		jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jFrame.setSize(864, 614);
		jFrame.setResizable(false);
		jFrame.setLocationRelativeTo(null);
		jFrame.setVisible(true);

		jFrame.getRootPane().setDefaultButton(jBtnJoinRoom);
		jBtnJoinRoom.requestFocus();

		ListeningEvents();
	}
}
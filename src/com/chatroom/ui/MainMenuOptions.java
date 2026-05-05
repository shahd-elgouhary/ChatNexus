package com.chatroom.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

public class MainMenuOptions {
	private JLabel jLabel;
	private JLabel jLabelTitle;
	private JFrame jFrame;
	private JButton jBtnCreateRoom;
	private JButton jBtnJoinRoom;
	private JButton jBtnViewRooms;
	private JButton jBtnLogout;
	private BufferedImage iconLogo;
	private ClientModel clientModel;
	private Request request;
	private Response response;

	public MainMenuOptions(ClientModel cm) throws IOException {
		clientModel = cm;
		jFrame = new JFrame("ChatNexus - Dashboard");
		com.chatroom.ui.icons.IconFactory.applyWindowIcon(jFrame);

		try {
			iconLogo = ImageIO.read(this.getClass().getResource("/logo.png"));
		} catch (Exception e) {}

		jFrame.setContentPane(new JPanel() {
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				g.setColor(Config.getBackground());
				g.fillRect(0, 0, getWidth(), getHeight());
			}
		});

		jBtnCreateRoom = new JButton("CREATE ROOM");
		jBtnJoinRoom = new JButton("JOIN ROOM");
		jBtnViewRooms = new JButton("VIEW ROOMS");
		jBtnLogout = new JButton("LOGOUT");

		jLabelTitle = new JLabel("Welcome to ChatNexus Dashboard");
		jLabelTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
		jLabelTitle.setForeground(Config.getTextPrimary());

		initializeAllWithProperties();
	}

	private void logOut() {
		new Thread(() -> {
			try {
				request = new Request(Request.Type.LOGOUT.ordinal(), clientModel.getClientID(), clientModel.getRoomId(), "");
				clientModel.getStreamManager().writeObject(request);
				response = (Response) clientModel.getStreamManager().readObject();
				if (response.getSuccess()) {
					clientModel.setRoomId(-1);
					clientModel.setClientID(-1);
					clientModel.getStreamManager().close();
					SwingUtilities.invokeLater(() -> {
						jFrame.dispose();
						// Re-initialize client model for new connection
						try {
							new SignInActivity(new ClientModel("127.0.0.1", 1234));
						} catch (IOException ex) {
							ex.printStackTrace();
						}
					});
				} else {
					SwingUtilities.invokeLater(() ->
						JOptionPane.showMessageDialog(null, response.getContents(), "Logout Error", JOptionPane.ERROR_MESSAGE)
					);
				}
			} catch (Exception e) {
				e.printStackTrace(new PrintWriter(Config.errors));
				LogFileWriter.Log(Config.errors.toString());
			}
		}, "Logout-Network").start();
	}

	private void ListeningEvents() {
		jBtnCreateRoom.addActionListener(e -> displayAlertDialog(1));
		jBtnJoinRoom.addActionListener(e -> displayAlertDialog(2));
		jBtnViewRooms.addActionListener(e -> {
			new Thread(() -> {
				try {
					ViewRoomsActivity vra = new ViewRoomsActivity(clientModel);
					SwingUtilities.invokeLater(() -> jFrame.dispose());
				} catch (Exception e1) {
					e1.printStackTrace(new PrintWriter(Config.errors));
					LogFileWriter.Log(Config.errors.toString());
				}
			}, "ViewRooms-Init").start();
		});
		jBtnLogout.addActionListener(e -> logOut());
		System.out.println("[Debug] MainMenuOptions action listeners attached successfully. Buttons enabled.");
	}

	private void setupButton(JButton btn, boolean isPrimary) {
		btn.setPreferredSize(new Dimension(250, 45));
		btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
		btn.setFocusPainted(false);
		btn.putClientProperty("JButton.buttonType", "roundRect");
		if (isPrimary) {
			btn.setBackground(Config.getPrimary());
			btn.setForeground(Color.white);
		} else {
			btn.setBackground(Config.getSurface());
			btn.setForeground(Config.getPrimary());
		}
	}

	private void initializeAllWithProperties() {
		setupButton(jBtnCreateRoom, true);
		setupButton(jBtnJoinRoom, true);
		setupButton(jBtnViewRooms, false);
		
		jBtnLogout.setPreferredSize(new Dimension(250, 45));
		jBtnLogout.setFont(new Font("Segoe UI", Font.BOLD, 14));
		jBtnLogout.setFocusPainted(false);
		jBtnLogout.putClientProperty("JButton.buttonType", "roundRect");
		jBtnLogout.setBackground(Config.getSurface());
		jBtnLogout.setForeground(Config.COLOR_ERROR);

		jFrame.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.CENTER;

		int gridy = 0;
		jLabel = new JLabel(com.chatroom.ui.icons.IconFactory.getLogoIcon(100));
		c.gridy = gridy++;
		c.insets = new Insets(0, 0, 10, 0);
		jFrame.add(jLabel, c);

		c.gridy = gridy++;
		c.insets = new Insets(0, 0, 30, 0);
		jFrame.add(jLabelTitle, c);

		c.gridy = gridy++;
		c.insets = new Insets(5, 0, 5, 0);
		jFrame.add(jBtnCreateRoom, c);

		c.gridy = gridy++;
		jFrame.add(jBtnJoinRoom, c);

		c.gridy = gridy++;
		jFrame.add(jBtnViewRooms, c);

		c.gridy = gridy++;
		c.insets = new Insets(30, 0, 10, 0);
		jFrame.add(jBtnLogout, c);

		jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jFrame.setSize(864, 614);
		jFrame.setResizable(false);
		jFrame.setLocationRelativeTo(null);
		jFrame.setVisible(true);

		ListeningEvents();
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
					// Extract room ID - try #N format first, fall back to finding any number
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
						// Fallback: extract first number from the response
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
		}, "Room-Network").start();
	}

	private void displayAlertDialog(int which) {
		JPanel jPanel = new JPanel();
		jPanel.setLayout(new BoxLayout(jPanel, BoxLayout.Y_AXIS));

		JTextField jTextField = new JTextField();
		jTextField.putClientProperty("JTextField.placeholderText", "Room Name");
		jTextField.setPreferredSize(new Dimension(250, 40));
		jTextField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		jPanel.add(jTextField);

		String title = (which == 1) ? "Create New Room" : "Join Existing Room";
		UIManager.put("OptionPane.okButtonText", (which == 1) ? "Create" : "Join");

		int choice = JOptionPane.showOptionDialog(jFrame, jPanel, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, null, null);
		if (choice == JOptionPane.OK_OPTION) {
			String roomName = jTextField.getText().trim();
			if (!roomName.isEmpty()) {
				createAndJoinRoom(roomName, which == 1);
			}
		}
	}
}

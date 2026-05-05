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
import com.chatroom.others.Hash;
import com.chatroom.others.LogFileWriter;

public class SignInActivity {
	private JLabel jLabel;
	private JFrame jFrame;
	private JButton jBtnSignIn;
	private JTextField jTvUsername;
	private JPasswordField jTvpassword;
	private JLabel jLabelSignup;
	private JLabel jLabelSignIntitle;
	private ClientModel clientModel;

	public SignInActivity(ClientModel cm) throws IOException {
		clientModel = cm;
		jFrame = new JFrame("ChatNexus - Sign In");
		com.chatroom.ui.icons.IconFactory.applyWindowIcon(jFrame);

		jFrame.setContentPane(new JPanel() {
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				g.setColor(Config.getBackground());
				g.fillRect(0, 0, getWidth(), getHeight());
			}
		});

		jBtnSignIn = new JButton("SIGN IN");
		jTvUsername = new JTextField();
		jTvpassword = new JPasswordField();

		jLabelSignIntitle = new JLabel("Welcome Back");
		jLabelSignIntitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
		jLabelSignIntitle.setForeground(Config.getTextPrimary());

		jLabelSignup = new JLabel("<html><a href=''>Not a user? Sign Up here</a></html>");
		jLabelSignup.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		jLabelSignup.setForeground(Config.getPrimary());

		initializeAllWithProperties();
	}

	private void ListeningEvents() {
		jLabelSignup.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				super.mouseClicked(e);
				try {
					jFrame.dispose();
					new SignUpActivity(clientModel);
				} catch (IOException e1) {
					e1.printStackTrace(new PrintWriter(Config.errors));
					LogFileWriter.Log(Config.errors.toString());
				}
			}
		});

		jBtnSignIn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String username = jTvUsername.getText();
				String password = new String(jTvpassword.getPassword());
				signIn(username, password);
			}
		});
	}

	private void signIn(String username, String password) {
		if (clientModel.getStreamManager() == null) {
			JOptionPane.showMessageDialog(jFrame, "Failed to connect to the server. Please check your connection.", "Connection Error", JOptionPane.ERROR_MESSAGE);
			return;
		}

		new Thread(() -> {
			String cont = username + "#" + Hash.getHash(password);
			Request request = new Request(Request.Type.LOGIN.ordinal(), clientModel.getClientID(), clientModel.getRoomId(), cont);
			try {
				clientModel.getStreamManager().writeObject(request);
				Response response = (Response) clientModel.getStreamManager().readObject();

				if (response.getId() == Response.Type.LOGIN.ordinal()) {
					if (response.getSuccess()) {
						clientModel.setClientID(Integer.parseInt(response.getContents()));
						SwingUtilities.invokeLater(() -> {
							try {
								new MainMenuOptions(clientModel);
							} catch (IOException ex) {
								ex.printStackTrace();
							}
							jFrame.dispose();
						});
					} else {
						SwingUtilities.invokeLater(() ->
							JOptionPane.showMessageDialog(null, response.getContents(), "Login Failed", JOptionPane.ERROR_MESSAGE)
						);
					}
				}
			} catch (IOException | ClassNotFoundException e) {
				e.printStackTrace(new PrintWriter(Config.errors));
				LogFileWriter.Log(Config.errors.toString());
			}
		}, "SignIn-Network").start();
	}

	private void initializeAllWithProperties() {
		jTvUsername.setPreferredSize(new Dimension(300, 40));
		jTvUsername.putClientProperty("JTextField.placeholderText", "Username");
		jTvUsername.setFont(new Font("Segoe UI", Font.PLAIN, 14));

		jTvpassword.setPreferredSize(new Dimension(300, 40));
		jTvpassword.putClientProperty("JTextField.placeholderText", "Password");
		jTvpassword.putClientProperty("JTextField.showRevealButton", true);
		jTvpassword.setFont(new Font("Segoe UI", Font.PLAIN, 14));

		jBtnSignIn.setPreferredSize(new Dimension(300, 45));
		jBtnSignIn.setBackground(Config.getPrimary());
		jBtnSignIn.setForeground(Color.white);
		jBtnSignIn.setFont(new Font("Segoe UI", Font.BOLD, 14));
		jBtnSignIn.setFocusPainted(false);
		jBtnSignIn.putClientProperty("JButton.buttonType", "roundRect");

		jFrame.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.CENTER;
		c.insets = new Insets(10, 0, 10, 0);

		int gridy = 0;
		jLabel = new JLabel(com.chatroom.ui.icons.IconFactory.getLogoIcon(200));
		c.gridy = gridy++;
		c.insets = new Insets(0, 0, 20, 0);
		jFrame.add(jLabel, c);

		c.gridy = gridy++;
		c.insets = new Insets(0, 0, 30, 0);
		jFrame.add(jLabelSignIntitle, c);

		c.gridy = gridy++;
		c.insets = new Insets(5, 0, 5, 0);
		jFrame.add(jTvUsername, c);

		c.gridy = gridy++;
		jFrame.add(jTvpassword, c);

		c.gridy = gridy++;
		c.insets = new Insets(25, 0, 10, 0);
		jFrame.add(jBtnSignIn, c);

		c.gridy = gridy++;
		c.insets = new Insets(10, 0, 0, 0);
		jFrame.add(jLabelSignup, c);

		jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jFrame.setSize(864, 614);
		jFrame.setResizable(false);
		jFrame.setLocationRelativeTo(null);
		jFrame.setVisible(true);

		jFrame.getRootPane().setDefaultButton(jBtnSignIn);
		jBtnSignIn.requestFocus();

		ListeningEvents();
	}
}

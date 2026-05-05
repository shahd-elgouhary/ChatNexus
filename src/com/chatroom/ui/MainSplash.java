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
import com.chatroom.others.LogFileWriter;

public class MainSplash {
	private JLabel jLabel;
	private JFrame jFrame;
	private JButton jBtnSignUp;
	private JButton jBtnSignIn;
	private BufferedImage iconLogo;
	private ClientModel clientModel;

	@SuppressWarnings("serial")
	public MainSplash(ClientModel cm) throws IOException {
		clientModel = cm;
		jFrame = new JFrame("ChatNexus");
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

		jBtnSignUp = new JButton("SIGN UP");
		jBtnSignIn = new JButton("SIGN IN");

		initializeAllWithProperties();
	}

	private void ListeningEvents() {
		jBtnSignUp.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					jFrame.dispose();
					new SignUpActivity(clientModel);
				} catch (IOException ex) {
					ex.printStackTrace(new PrintWriter(Config.errors));
					LogFileWriter.Log(Config.errors.toString());
				}
			}
		});

		jBtnSignIn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					jFrame.dispose();
					new SignInActivity(clientModel);
				} catch (IOException ex) {
					ex.printStackTrace(new PrintWriter(Config.errors));
					LogFileWriter.Log(Config.errors.toString());
				}
			}
		});
	}

	private void initializeAllWithProperties() {
		jBtnSignUp.setPreferredSize(new Dimension(250, 45));
		jBtnSignUp.setBackground(Config.getPrimary());
		jBtnSignUp.setForeground(Color.white);
		jBtnSignUp.setFont(new Font("Segoe UI", Font.BOLD, 14));
		jBtnSignUp.setFocusPainted(false);
		jBtnSignUp.putClientProperty("JButton.buttonType", "roundRect");

		jBtnSignIn.setPreferredSize(new Dimension(250, 45));
		jBtnSignIn.setBackground(Config.getSurface());
		jBtnSignIn.setForeground(Config.getPrimary());
		jBtnSignIn.setFont(new Font("Segoe UI", Font.BOLD, 14));
		jBtnSignIn.setFocusPainted(false);
		jBtnSignIn.putClientProperty("JButton.buttonType", "roundRect");

		jFrame.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		Insets buttonInsets = new Insets(10, 0, 10, 0);

		c.anchor = GridBagConstraints.CENTER;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.gridy = 1;

		jLabel = new JLabel(com.chatroom.ui.icons.IconFactory.getLogoIcon(200));
		c.insets = new Insets(0, 0, 40, 0);
		jFrame.add(jLabel, c);

		c.gridy = 2;
		c.insets = buttonInsets;
		jFrame.add(jBtnSignUp, c);

		c.gridy = 3;
		jFrame.add(jBtnSignIn, c);

		jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jFrame.setSize(864, 614);
		jFrame.setLocationRelativeTo(null);
		jFrame.setVisible(true);
		jFrame.setResizable(false);

		ListeningEvents();
	}
}

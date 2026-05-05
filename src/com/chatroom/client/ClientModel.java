package com.chatroom.client;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import com.chatroom.network.StreamManager;
import com.chatroom.others.LogFileWriter;

public class ClientModel {
	private int clientID=-1;
	private int roomId = -1;
	private String host = "";
	private int port = -1;
	private StreamManager streamManager;
	private Socket socket;
	private boolean connected = false;
	
	public ClientModel(String host, int port) {
		super();
		this.host = host;
		this.port = port;
		
		// connecting with timeout
		try {
			socket = new Socket();
			socket.connect(new InetSocketAddress(host, port), 5000); // 5 second connection timeout
			streamManager = new StreamManager(socket);
			streamManager.initializeStreams();
			connected = true;
			System.out.println("[ClientModel] Connected to server " + host + ":" + port);
		} catch (IOException e) {
			System.err.println("[ClientModel] Failed to connect to server " + host + ":" + port + " - " + e.getMessage());
			streamManager = null;
			connected = false;
		}
	}
	
	public StreamManager getStreamManager() {
		return streamManager;
	}

	public boolean isConnected() {
		return connected && streamManager != null;
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	public int getClientID() {
		return clientID;
	}

	public void setClientID(int clientID) {
		this.clientID = clientID;
	}

	public int getRoomId() {
		return roomId;
	}

	public void setRoomId(int roomId) {
		this.roomId = roomId;
	}

}

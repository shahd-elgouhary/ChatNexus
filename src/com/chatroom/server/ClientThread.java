package com.chatroom.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import com.chatroom.network.StreamManager;

import com.chatroom.configuration.Config;
import com.chatroom.models.Request;
import com.chatroom.others.LogFileWriter;
import com.chatroom.others.Message;

public class ClientThread extends Thread {

	Socket socket;
	StreamManager streamManager;
	Request request;

	public ClientThread(Socket s) {
		socket = s;
	}

	public void run() {
		try {
			streamManager = new StreamManager(socket);
			streamManager.initializeStreams();

			while (true) {
				request = (Request) streamManager.readObject();
				Server.requestqueue.add(this);
				if (Server.requestAnalyser.getState() == State.WAITING) {
					synchronized (Server.requestAnalyser) {
						Server.requestAnalyser.notify();
					}
				}
			}

		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace(new PrintWriter(Config.errors));
			LogFileWriter.Log(Config.errors.toString());
			if (e.getClass() == java.io.EOFException.class) {
				// if client exited the terminal itself
				if (request.getRoomId() != -1) {
					this.request.setContents("sv_logout");
					this.request.setId(Request.Type.MSG.ordinal());
					Server.requestqueue.add(this);
				} else {
					this.request.setContents("");
					this.request.setId(Request.Type.LOGOUT.ordinal());
					this.request.setRoomId(-1);
					Server.requestqueue.add(this);
				}

				if (Server.requestAnalyser.getState() == State.WAITING) {
					synchronized (Server.requestAnalyser) {
						Server.requestAnalyser.notify();
					}
				}
			}
		}
	}
}

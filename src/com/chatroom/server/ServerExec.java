package com.chatroom.server;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import com.chatroom.Database.createdb;
import com.chatroom.configuration.Config;
import com.chatroom.others.LogFileWriter;
import com.chatroom.others.Message;

public class ServerExec {

	public static void main(String[] args) {
		System.out.println("STEP 1: Main started");
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

		//updating the configuration
		if (args.length >= 4) {
			Config.DATABASE_HOST = args[1];
			Config.USER_NAME = args[2];
			Config.USER_PWD = args[3];
		}
		System.out.println("STEP 2: Arguments parsed");

		boolean dbOk = false;
		try {
			new createdb();
			dbOk = true;
			Message.println("Database connected successfully.");
		} catch (Exception e) {
			Message.println("WARNING: Database connection failed (" + e.getMessage() + ")");
			Message.println("WARNING: Running in IN-MEMORY mode. Sign Up/Sign In will use simple in-memory storage.");
			Message.println("WARNING: User data will be lost when the server stops.");
		}
		System.out.println("STEP 3: Database initialized");

		try {
			if (!dbOk) {
				Server.useInMemoryAuth = true;
			}
			Server server = new Server(args.length > 0 ? Integer.parseInt(args[0]) : 1234);
			System.out.println("STEP 4: Server constructor called");
			System.out.println("STEP 5: About to bind socket");
			server.connect();
		} catch (Exception e) {
			System.err.println("Failed to start server: " + e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}
	}
}

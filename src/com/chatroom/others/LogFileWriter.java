package com.chatroom.others;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LogFileWriter {
	// prints the exception messages to the console
	public static void Log(String message) {
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		System.out.println(dateFormat.format(date) + "\t" + message);
	}
}

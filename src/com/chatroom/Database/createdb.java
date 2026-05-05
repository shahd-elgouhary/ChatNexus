package com.chatroom.Database;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import com.chatroom.configuration.Config;
import com.chatroom.others.LogFileWriter;
import com.chatroom.others.Message;


public class createdb {
	public createdb() throws Exception {
		Connection connection = null;
		java.sql.Statement statement= null;
		
		try {
			Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
			//creating database	
			String baseUrl = Config.DATABASE_URL + Config.DATABASE_HOST + Config.DATABASE_PORT + "?connectTimeout=3000&socketTimeout=3000";
			connection = DriverManager.getConnection(baseUrl, Config.USER_NAME, Config.USER_PWD);
			String Query = "CREATE DATABASE IF NOT EXISTS "+ Config.DATABASE_NAME;
			statement = connection.createStatement();
			statement.executeUpdate(Query);
			
			//for execute multiple queries separate queries by semicolon
			String dbUrl = Config.DATABASE_URL + Config.DATABASE_HOST + Config.DATABASE_PORT + '/' + Config.DATABASE_NAME + "?allowMultiQueries=true&connectTimeout=3000&socketTimeout=3000";
			connection = DriverManager.getConnection(dbUrl, Config.USER_NAME, Config.USER_PWD);
			String Queries = "CREATE TABLE IF NOT EXISTS " + Config.TABLE_NAME + "(" + Config.CLIENT_ID + " int auto_increment," + Config.CLIENT_NAME + " VARCHAR(50) not null, "+ Config.CLIENT_PWD + " VARCHAR(150), " +"primary key(" +Config.CLIENT_ID+ "))";
			
			statement = connection.createStatement();
			statement.executeUpdate(Queries);
			
		} catch (Exception e) {
			e.printStackTrace(new PrintWriter(Config.errors));
			LogFileWriter.Log(Config.errors.toString());
			throw e;
		}
		finally {
			if (connection != null) {
				try {
					connection.close(); //close the database connection
				} catch (SQLException e) {
					e.printStackTrace(new PrintWriter(Config.errors));
					LogFileWriter.Log(Config.errors.toString());
				}
			}
		}
	}
}

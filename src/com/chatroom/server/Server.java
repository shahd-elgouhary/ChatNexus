package com.chatroom.server;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import com.chatroom.network.StreamManager;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Scanner;
import java.util.Set;
import com.chatroom.configuration.Config;
import com.chatroom.models.MessageTrackObject;
import com.chatroom.models.Request;
import com.chatroom.models.Response;
import com.chatroom.others.LogFileWriter;
import com.chatroom.others.Message;

class RequestAnalyser extends Thread {
	String name = null;
	ResultSet resultSet = null;
	java.sql.PreparedStatement preparedStatement = null;
	String query = null;
	Set<Integer> clientIds;
	MessageTrackObject messageTrackObject;

	public void run() {
		try {

			Response response = null;
			Request request = null;
			ClientThread clientThread = null;
			while (true) {
				if (Server.requestqueue.isEmpty())
					synchronized (this) {
						this.wait();
					}
				clientThread = Server.requestqueue.poll();
				request = clientThread.request;
				switch (request.getId()) {
					case 1:
						int clientID = -1;

						String requestContent = request.getContents().trim();
						String username = requestContent.substring(0, requestContent.indexOf("#"));

						if (Server.useInMemoryAuth) {
							// In-memory signup
							if (Server.inMemoryUsers.containsKey(username)) {
								response = new Response(1, false, "User already present ...");
							} else {
								String pwd = requestContent.substring(requestContent.indexOf("#") + 1);
								clientID = Server.nextInMemoryId++;
								Server.inMemoryUsers.put(username, pwd);
								Server.inMemoryUserIds.put(username, clientID);
								Server.inMemoryIdNames.put(clientID, username);
								response = new Response(1, true, String.valueOf(clientID));
								Server.clientHolder.put(clientID, clientThread);
								Server.messagesTrackHashmap.put(clientID, 0);
							}
							Server.responseMakerQueue.add(new ResponseHolder(response, clientThread.streamManager));
							if (Server.responseMaker.getState() == State.WAITING) {
								synchronized (Server.responseMaker) {
									Server.responseMaker.notify();
								}
							}
							break;
						}

						// first check if the user already exists or not
						query = "SELECT " + Config.CLIENT_ID + " from " + Config.TABLE_NAME + " WHERE "
								+ Config.CLIENT_NAME + "=?";
						preparedStatement = Server.connection.prepareStatement(query);
						preparedStatement.setString(1, username);
						resultSet = preparedStatement.executeQuery();

						if (!resultSet.isBeforeFirst()) {
							// if user is not already present then insert data into database
							String query = "INSERT INTO " + Config.TABLE_NAME + "(" + Config.CLIENT_NAME + ","
									+ Config.CLIENT_PWD + ") VALUES(?,?)";
							String pwd = requestContent.substring(requestContent.indexOf("#") + 1);
							preparedStatement = Server.connection.prepareStatement(query,
									Statement.RETURN_GENERATED_KEYS);
							preparedStatement.setString(1, username);
							preparedStatement.setString(2, pwd);

							int response_code = preparedStatement.executeUpdate();
							if (response_code > 0) {
								// success
								resultSet = preparedStatement.getGeneratedKeys();
								if (resultSet.next())
									clientID = resultSet.getInt(1);
								response = new Response(1, true, String.valueOf(clientID));
								// store the client in our client holder hash map
								Server.clientHolder.put(clientID, clientThread);
								// update the user message count
								Server.messagesTrackHashmap.put(clientID, 0);
							} else {
								// something went wrong or if data is not inserted in database
								response = new Response(1, false, "Something went wrong while signing up ...");
							}
						} else {
							response = new Response(1, false, "User already present ...");
						}

						Server.responseMakerQueue.add(new ResponseHolder(response, clientThread.streamManager));
						if (Server.responseMaker.getState() == State.WAITING) {
							synchronized (Server.responseMaker) {
								Server.responseMaker.notify();
							}
						}
						break;
					case 2:
						requestContent = request.getContents().trim();
						String name = requestContent.substring(0, requestContent.indexOf("#"));
						String pwd = requestContent.substring(requestContent.indexOf("#") + 1);

						if (Server.useInMemoryAuth) {
							// In-memory login
							if (Server.inMemoryUsers.containsKey(name) && Server.inMemoryUsers.get(name).equals(pwd)) {
								int memId = Server.inMemoryUserIds.get(name);
								response = new Response(2, true, String.valueOf(memId));
								Server.clientHolder.put(memId, clientThread);
								Server.messagesTrackHashmap.put(memId, 0);
							} else {
								response = new Response(2, false, "Check your username and password ...");
							}
							Server.responseMakerQueue.add(new ResponseHolder(response, clientThread.streamManager));
							if (Server.responseMaker.getState() == State.WAITING) {
								synchronized (Server.responseMaker) {
									Server.responseMaker.notify();
								}
							}
							break;
						}

						query = "SELECT " + Config.CLIENT_ID + " from " + Config.TABLE_NAME + " WHERE "
								+ Config.CLIENT_NAME + "=? AND " + Config.CLIENT_PWD + "=?";
						preparedStatement = Server.connection.prepareStatement(query);
						preparedStatement.setString(1, name);
						preparedStatement.setString(2, pwd);

						ResultSet resultSet = preparedStatement.executeQuery();
						if (!resultSet.isBeforeFirst()) {
							// no data
							response = new Response(2, false, "Check your username and password ...");
						} else {
							resultSet.next();
							int client_id = resultSet.getInt(1);
							response = new Response(2, true, String.valueOf(client_id));
							// store the client in our client holder hash map
							Server.clientHolder.put(client_id, clientThread);

							// stores the client id and message count into the hash map
							Server.messagesTrackHashmap.put(client_id, 0);
						}

						Server.responseMakerQueue.add(new ResponseHolder(response, clientThread.streamManager));
						if (Server.responseMaker.getState() == State.WAITING) {
							synchronized (Server.responseMaker) {
								Server.responseMaker.notify();
							}
						}
						break;
					case 3:
						// for logout
						logout(clientThread, request, 1);
						break;
					case 4:
						// create room
						int roomId = Server.getRoomId();
						String roomName = request.getContents();

						if (Server.roomsMapping.containsValue(roomName)) {
							// if the room name is already present
							response = new Response(4, false, "Room name already exists ...");
						} else {
							// insert the new mapping if it is not already present in the hash map
							Server.roomsMapping.put(roomId, roomName);
							Server.listOfRooms.add(roomId);
							clientIds = new HashSet<>();
							clientIds.add(request.getClientId()); // insert the client into the set
							Server.roomsHolder.put(roomId, clientIds);
							response = new Response(4, true, "Room #" + roomId + " created and joined successfully");
						}
						Server.responseMakerQueue.add(new ResponseHolder(response, clientThread.streamManager));
						if (Server.responseMaker.getState() == State.WAITING) {
							synchronized (Server.responseMaker) {
								Server.responseMaker.notify();
							}
						}
						break;
					case 5:
						// join room
						String roomName1 = request.getContents();
						int clientId = request.getClientId();
						int roomId1 = -1;
						roomId1 = Server.getKey(roomName1);

						if (Server.listOfRooms.contains(roomId1)) {
							// insert the client id in the set of the specific room id in hash map
							Server.roomsHolder.get(roomId1).add(clientId);
							response = new Response(Response.Type.JOIN_ROOM.ordinal(), true,
									"Room #" + roomId1 + " joined successfully ...");
						} else {
							response = new Response(5, false, "Room not found ...");
						}
						Server.responseMakerQueue.add(new ResponseHolder(response, clientThread.streamManager));
						if (Server.responseMaker.getState() == State.WAITING) {
							synchronized (Server.responseMaker) {
								Server.responseMaker.notify();
							}
						}
						break;
					case 6:
						// list of rooms
						String roomNames = "";
						if (Server.listOfRooms.size() > 0) {
							Iterator<Integer> iterator = Server.listOfRooms.iterator();
							while (iterator.hasNext()) {
								// get the room names from hash map
								roomNames += Server.roomsMapping.get(iterator.next()) + "\n";
							}
							response = new Response(6, true, roomNames);
						} else {
							response = new Response(6, false, "Currently there are no active rooms ...");
						}
						Server.responseMakerQueue.add(new ResponseHolder(response, clientThread.streamManager));
						if (Server.responseMaker.getState() == State.WAITING) {
							synchronized (Server.responseMaker) {
								Server.responseMaker.notify();
							}
						}
						break;

					case 7:
						// message
						if (request.getContents().equals(" ") || request.getContents().equals(""))
							break;
						System.out.println("[DEBUG] MESSAGE RECEIVED BY SERVER: " + request.getContents());
						Server.messagequeue.add(request);
						if (Server.messageHandler.getState() == State.WAITING) {
							synchronized (Server.messageHandler) {
								Server.messageHandler.notify();
							}
						}
						break;
					case 8:
						if (request.getContents().equals(" ") || request.getContents().equals(""))
							break;
						Server.messagequeue.add(request);
						if (Server.messageHandler.getState() == State.WAITING) {
							synchronized (Server.messageHandler) {
								Server.messageHandler.notify();
							}
						}
						break;

					default:
						Message.println("Invalid");
				}
			}
		} catch (Exception e) {
			e.printStackTrace(new PrintWriter(Config.errors));
			LogFileWriter.Log(Config.errors.toString());
		}
	}

	public static void logout(ClientThread clientThread, Request request, int temp) {
		Response response = new Response(Response.Type.LOGOUT.ordinal(), true, "Logout successfully");
		Server.responseMakerQueue.add(new ResponseHolder(response, clientThread.streamManager));
		if (temp == 1) {
			if (request.getRoomId() != -1)
				Server.roomsHolder.get(request.getRoomId()).remove(request.getClientId());
			Server.clientHolder.remove(request.getClientId());
		}

		if (Server.responseMaker.getState() == State.WAITING) {
			synchronized (Server.responseMaker) {
				Server.responseMaker.notify();
			}
		}

		if (request.getClientId() != -1)
			Message.println(Server.getClientNameFromId(request.getClientId()) + " logged out sucessfully");
		else
			Message.println(request.getClientId() + " logged out sucessfully");

	}
}

class ResponseMaker extends Thread {

	public void run() {
		ResponseHolder responseHolder = null;

		while (true) {
			try {
				if (Server.responseMakerQueue.isEmpty())
					synchronized (this) {
						this.wait();
					}
				responseHolder = Server.responseMakerQueue.poll();
				responseHolder.streamManager.writeObject(responseHolder.response);
			} catch (Exception e) {
				e.printStackTrace(new PrintWriter(Config.errors));
				LogFileWriter.Log(Config.errors.toString());
			}
		}
	}
}

class MessageHandler extends Thread {

	public void run() {
		Request request;
		ResultSet resultSet = null;
		java.sql.PreparedStatement preparedStatement = null;
		String query = null;
		String msg = "";
		String sender = "";
		String reciever = "";
		String data = "";
		int recieverId = -1;
		while (true) {
			data = "";
			try {
				if (Server.messagequeue.isEmpty())
					synchronized (this) {
						this.wait();
					}
				request = Server.messagequeue.poll();

				if (Server.useInMemoryAuth || Server.connection == null) {
					System.out.println("[DEBUG] IN-MEMORY MESSAGE MODE");
					sender = Server.inMemoryIdNames.getOrDefault(request.getClientId(), "User" + request.getClientId());
				} else {
					query = "SELECT " + Config.CLIENT_NAME + " from " + Config.TABLE_NAME + " WHERE " + Config.CLIENT_ID
							+ "=?";
					preparedStatement = Server.connection.prepareStatement(query);
					preparedStatement.setInt(1, request.getClientId());
					resultSet = preparedStatement.executeQuery();

					if (resultSet.next())
						sender = resultSet.getString(1);
				}

				Set<Integer> set = Server.roomsHolder.get(request.getRoomId());

				// update the user message count

				if (!request.getContents().equals("sv_typing")) {
					Server.messagesTrackHashmap.put(request.getClientId(),
							Server.messagesTrackHashmap.get(request.getClientId()) + 1);
				}

				boolean personalMessage = request.getContents().indexOf("@") != -1 ? true : false;
				if (personalMessage) {
					reciever = request.getContents().substring(request.getContents().indexOf("@") + 1,
							request.getContents().indexOf(" "));
					
					if (Server.useInMemoryAuth || Server.connection == null) {
						recieverId = Server.inMemoryUserIds.getOrDefault(reciever, -1);
					} else {
						query = "SELECT " + Config.CLIENT_ID + " from " + Config.TABLE_NAME + " WHERE " + Config.CLIENT_NAME
								+ "=?";
						preparedStatement = Server.connection.prepareStatement(query);
						preparedStatement.setString(1, reciever);
						resultSet = preparedStatement.executeQuery();
						if (!resultSet.isBeforeFirst()) {
							recieverId = -1;
						} else {
							resultSet.next();
							recieverId = resultSet.getInt(1);
						}
					}
					if (recieverId != -1) {
						if (recieverId == request.getClientId())
							continue;

						ClientThread ct = Server.clientHolder.get(recieverId); // gives the client thread object
						StreamManager oos = ct.streamManager;
						msg = sender + " " + request.getContents().substring(request.getContents().indexOf(" "));
						Response res = new Response(Response.Type.P_MSG.ordinal(), true, msg);
						oos.writeObject(res);
						continue;
					} else {
						// user name wrong
						ClientThread ct = Server.clientHolder.get(request.getClientId()); // gives the client thread
																							// object
						StreamManager oos = ct.streamManager;
						Response res = null;
						msg = "Wrong username " + reciever;
						res = new Response(Response.Type.STATUS_MSG.ordinal(), true, msg);
						oos.writeObject(res);
						continue;

					}
				}

				System.out.println("[DEBUG] BROADCASTING TO " + set.size() + " CLIENTS");
				Iterator<Integer> iterator = set.iterator();
				while (iterator.hasNext()) {
					int id = iterator.next();
					// take id and check if its not sender and then create response
					// if(id != request.getClientId()) {
					try {
						if (request.getContents().equals("sv_exit") || request.getContents().equals("sv_logout")) {
							msg = sender + " has left the chat\n";
							if (id != request.getClientId()) {
								ClientThread ct = Server.clientHolder.get(id); // gives the client thread object
								try {
									StreamManager oos = ct.streamManager;
									Response res = new Response(Response.Type.STATUS_MSG.ordinal(), true, msg);
									oos.writeObject(res);
								} catch (Exception e) {
									e.printStackTrace(new PrintWriter(Config.errors));
									LogFileWriter.Log(Config.errors.toString());
								}
							} else {
								ClientThread ct = Server.clientHolder.get(id); // gives the client thread object
								try {
									if (request.getContents().equals("sv_exit")) {
										StreamManager oos = ct.streamManager;
										Response res = new Response(Response.Type.STATUS_MSG.ordinal(), true,
												"sv_exit_successful");
										oos.writeObject(res);
									}
								} catch (Exception e) {
									e.printStackTrace(new PrintWriter(Config.errors));
									LogFileWriter.Log(Config.errors.toString());
								} finally {
									if (request.getContents().equals("sv_logout")) {
										RequestAnalyser.logout(ct, request, 0);
									}
								}
							}
						} else if (request.getContents().equals("sv_showusers")) {
							// show the current online users in that room
							if (id != request.getClientId())
								data += Server.getClientNameFromId(id) + ",";
						} else {
							if (((request.getId() == Request.Type.STATUS_MSG.ordinal()) || id != request.getClientId())
									|| request.getIsConsole()) {
								if (request.getIsConsole()) {
									msg = sender + " " + request.getContents();
								} else {
									if (id == request.getClientId())
										msg = sender + " " + request.getContents() + " Active Users: " + set.size();
									else
										msg = sender + " " + request.getContents();
								}
								ClientThread ct = Server.clientHolder.get(id); // gives the client thread object
								if (ct != null) {
									StreamManager oos = ct.streamManager;
									Response res = null;
									if (request.getId() == Request.Type.STATUS_MSG.ordinal())
										res = new Response(Response.Type.STATUS_MSG.ordinal(), true, msg);
									else
										res = new Response(Response.Type.MSG.ordinal(), true, msg);
									System.out.println("[DEBUG] BROADCASTING TO CLIENT " + id + ": " + msg);
									oos.writeObject(res);
								}
							}

						}

					} catch (Exception e) {
						e.printStackTrace(new PrintWriter(Config.errors));
						LogFileWriter.Log(Config.errors.toString());
					}
				}
				if (request.getContents().equals("sv_exit")) {
					Set<Integer> set1 = Server.roomsHolder.get(request.getRoomId());
					set1.remove(request.getClientId());
				} else if (request.getContents().equals("sv_logout")) {
					Server.roomsHolder.get(request.getRoomId()).remove(request.getClientId());
					Server.clientHolder.remove(request.getClientId());
				} else if (request.getContents().equals("sv_showusers")) {
					ClientThread ct = Server.clientHolder.get(request.getClientId());
					StreamManager oos = ct.streamManager;
					Response response = new Response(Response.Type.GEN.ordinal(), true, data);
					oos.writeObject(response);
				}
			} catch (Exception e) {
				e.printStackTrace(new PrintWriter(Config.errors));
				LogFileWriter.Log(Config.errors.toString());
			}
		}
	}
}

class MessagesTrackComparator implements Comparator<MessageTrackObject> {

	@Override
	public int compare(MessageTrackObject o1, MessageTrackObject o2) {
		// TODO Auto-generated method stub
		if (o1.getCount() > o2.getCount()) {
			return -1;
		} else if (o1.getCount() < o2.getCount())
			return 1;
		else
			return 0;
	}
}

public class Server {
	Scanner scanner = new Scanner(System.in);
	int choice;
	ServerSocket serverSocket;
	Socket socket;
	int port = -1;
	Request request;
	Response response;
	static Queue<ClientThread> requestqueue;
	static Queue<ResponseHolder> responseMakerQueue;
	static Queue<Request> messagequeue;
	static RequestAnalyser requestAnalyser;
	static ResponseMaker responseMaker;
	static MessageHandler messageHandler;
	static Connection connection = null;
	static ArrayList<Integer> listOfRooms;
	static HashMap<Integer, Set<Integer>> roomsHolder;
	static HashMap<Integer, String> roomsMapping;
	static HashMap<Integer, ClientThread> clientHolder; // hold the clients with their unique id
	static PriorityQueue<MessageTrackObject> messagesTrackQueue; // holds the log of which user sends how many messages
	static HashMap<Integer, Integer> messagesTrackHashmap;
	static ServerOperations serverOperations;
	static int roomIdGenerator = 0;
	static boolean useInMemoryAuth = false;
	static HashMap<String, String> inMemoryUsers = new HashMap<>();
	static HashMap<String, Integer> inMemoryUserIds = new HashMap<>();
	static HashMap<Integer, String> inMemoryIdNames = new HashMap<>();
	static int nextInMemoryId = 1;

	public Server(int port) {
		this.port = port;
		requestqueue = new LinkedList<>();
		messagequeue = new LinkedList<>();
		responseMakerQueue = new LinkedList<>();
		requestAnalyser = new RequestAnalyser();
		requestAnalyser.start();
		responseMaker = new ResponseMaker();
		responseMaker.start();
		messageHandler = new MessageHandler();
		messageHandler.start();
		roomsHolder = new HashMap<>(); // for mapping the room id's and client id's in particular room
		roomsMapping = new HashMap<>();
		listOfRooms = new ArrayList<>();
		clientHolder = new HashMap<>();
		messagesTrackHashmap = new HashMap<>();
		messagesTrackQueue = new PriorityQueue<MessageTrackObject>(100, new MessagesTrackComparator());
		serverOperations = new ServerOperations();
		serverOperations.start();

		try {
			if (!useInMemoryAuth) {
				Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
				String dbUrl = Config.DATABASE_URL + Config.DATABASE_HOST + Config.DATABASE_PORT + "/" + Config.DATABASE_NAME + "?connectTimeout=3000&socketTimeout=3000";
				connection = DriverManager.getConnection(dbUrl, Config.USER_NAME, Config.USER_PWD);
			}
		} catch (SQLException | ClassNotFoundException e) {
			e.printStackTrace(new PrintWriter(Config.errors));
			LogFileWriter.Log(Config.errors.toString());
		} catch (InstantiationException e) {
			e.printStackTrace(new PrintWriter(Config.errors));
			LogFileWriter.Log(Config.errors.toString());
		} catch (IllegalAccessException e) {
			e.printStackTrace(new PrintWriter(Config.errors));
			LogFileWriter.Log(Config.errors.toString());
		}
	}

	@Override
	protected void finalize() {
		try {
			if (connection != null) connection.close();
		} catch (SQLException e) {
			e.printStackTrace(new PrintWriter(Config.errors));
			LogFileWriter.Log(Config.errors.toString());
		}
	}

	public static int getRoomId() {
		return roomIdGenerator++;
	}

	public static void shutdown() {
		Message.println("Server stopped");
		System.exit(1);
	}

	public static String getClientNameFromId(int id) {
		if (useInMemoryAuth) {
			String n = inMemoryIdNames.get(id);
			return n != null ? n : "User" + id;
		}
		String name;
		try {
			String dbUrl = Config.DATABASE_URL + Config.DATABASE_HOST + Config.DATABASE_PORT + "/" + Config.DATABASE_NAME + "?connectTimeout=3000&socketTimeout=3000";
			Connection connection = DriverManager.getConnection(dbUrl, Config.USER_NAME, Config.USER_PWD);
			String sql = "select client_name from users where client_id = ?";
			java.sql.PreparedStatement preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setInt(1, id);
			ResultSet resultSet = preparedStatement.executeQuery();
			resultSet.next();
			name = resultSet.getString(1);
			return name;

		} catch (SQLException e) {
			e.printStackTrace(new PrintWriter(Config.errors));
			LogFileWriter.Log(Config.errors.toString());
			return "User" + id;
		}

	}

	public static Integer getKey(String value) {
		for (Entry<Integer, String> entry : roomsMapping.entrySet()) {
			if (entry.getValue().equals(value)) {
				return entry.getKey();
			}
		}
		return -1;
	}

	public void connect() {
		try {
			serverSocket = new ServerSocket(port);
			System.out.println("STEP 6: Server listening");
			Message.println("Server successfully started and listening on port " + port);
			while (true) {
				socket = serverSocket.accept();
				System.out.println("[DEBUG] CLIENT CONNECTED: " + socket.getInetAddress());
				new ClientThread(socket).start();

			}
		} catch (IOException e) {
			System.err.println("CRITICAL ERROR: Failed to bind to port " + port);
			e.printStackTrace();
			System.exit(1);
		}
	}

}
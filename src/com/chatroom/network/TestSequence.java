package com.chatroom.network;

import java.io.IOException;
import java.net.Socket;
import com.chatroom.models.Request;
import com.chatroom.models.Response;

public class TestSequence {
    public static void main(String[] args) throws Exception {
        System.out.println("Starting Test Sequence...");
        
        // Connect Client A
        Socket socketA = new Socket("127.0.0.1", 1234);
        StreamManager smA = new StreamManager(socketA);
        smA.initializeStreams();
        System.out.println("[Client A] Connected.");

        // Connect Client B
        Socket socketB = new Socket("127.0.0.1", 1234);
        StreamManager smB = new StreamManager(socketB);
        smB.initializeStreams();
        System.out.println("[Client B] Connected.");
        
        // Client A login
        String contA = "User1#d41d8cd98f00b204e9800998ecf8427e"; // Hash for empty password or dummy
        smA.writeObject(new Request(Request.Type.LOGIN.ordinal(), -1, -1, contA));
        Response resA = (Response) smA.readObject();
        int clientIdA = Integer.parseInt(resA.getContents());
        System.out.println("[Client A] Logged in as User1. Client ID: " + clientIdA);

        // Client B login
        String contB = "User2#d41d8cd98f00b204e9800998ecf8427e";
        smB.writeObject(new Request(Request.Type.LOGIN.ordinal(), -1, -1, contB));
        Response resB = (Response) smB.readObject();
        int clientIdB = Integer.parseInt(resB.getContents());
        System.out.println("[Client B] Logged in as User2. Client ID: " + clientIdB);

        // Client A creates room
        smA.writeObject(new Request(Request.Type.CREATE_ROOM.ordinal(), clientIdA, -1, "TestRoom"));
        Response resRoomA = (Response) smA.readObject();
        int roomId = Integer.parseInt(resRoomA.getContents().replaceAll("[^0-9]", ""));
        System.out.println("[Client A] Created room TestRoom. Room ID: " + roomId);

        // Client B joins room
        smB.writeObject(new Request(Request.Type.JOIN_ROOM.ordinal(), clientIdB, -1, "TestRoom"));
        Response resRoomB = (Response) smB.readObject();
        System.out.println("[Client B] Joined TestRoom: " + resRoomB.getContents());

        // Client A sends message
        smA.writeObject(new Request(Request.Type.MSG.ordinal(), clientIdA, roomId, "Hello User2"));
        System.out.println("[Client A] Sent message: Hello User2");

        // Client B reads message
        Response msgFromA = (Response) smB.readObject();
        System.out.println("[Client B] Received: " + msgFromA.getContents());

        // Client B sends reply
        smB.writeObject(new Request(Request.Type.MSG.ordinal(), clientIdB, roomId, "Hello User1"));
        System.out.println("[Client B] Sent reply: Hello User1");

        // Client A reads reply
        Response msgFromB = (Response) smA.readObject();
        System.out.println("[Client A] Received: " + msgFromB.getContents());

        // Client A exits room
        smA.writeObject(new Request(Request.Type.MSG.ordinal(), clientIdA, roomId, "sv_exit"));
        Response exitResA = (Response) smA.readObject();
        System.out.println("[Client A] Exited room: " + exitResA.getContents());

        // Client A disconnects
        smA.close();
        smB.close();
        System.out.println("Test Sequence Complete & Successful!");
    }
}

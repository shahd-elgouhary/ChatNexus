package com.chatroom.network;

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import com.chatroom.configuration.Config;
import com.chatroom.others.LogFileWriter;

public class StreamManager {
    private final Socket socket;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private boolean streamsInitialized = false;
    private int successfulWrites = 0;
    private final Object writeLock = new Object();
    private final Object readLock = new Object();
    
    public StreamManager(Socket socket) {
        this.socket = socket;
    }
    
    public void initializeStreams() throws IOException {
        if (!streamsInitialized) {
            // Important: Create OOS first then OIS
            oos = new ObjectOutputStream(socket.getOutputStream());
            oos.flush();
            ois = new ObjectInputStream(socket.getInputStream());
            streamsInitialized = true;
            System.out.println("[StreamManager] Streams initialized successfully for socket: " + socket.getRemoteSocketAddress());
        }
    }
    
    public void writeObject(Object obj) throws IOException {
        if (oos == null) throw new IOException("Output stream not initialized");
        
        synchronized (writeLock) {
            try {
                System.out.println("[StreamManager] Attempting to write object of type: " + obj.getClass().getSimpleName());
                oos.reset(); // Reset to avoid reference caching
                oos.writeObject(obj);
                oos.flush();
                successfulWrites++;
                System.out.println("[StreamManager] Write successful. Total writes: " + successfulWrites);
            } catch (IOException e) {
                System.err.println("[StreamManager] Exception during writeObject: " + e.getMessage());
                e.printStackTrace(new PrintWriter(Config.errors));
                LogFileWriter.Log(Config.errors.toString());
                throw e;
            }
        }
    }
    
    public Object readObject() throws IOException, ClassNotFoundException {
        if (ois == null) throw new IOException("Input stream not initialized");
        
        synchronized (readLock) {
            try {
                return ois.readObject();
            } catch (SocketTimeoutException e) {
                System.err.println("[StreamManager] Read timed out: " + e.getMessage());
                throw e;
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("[StreamManager] Exception during readObject: " + e.getMessage());
                throw e;
            }
        }
    }
    
    public void close() {
        try { if (oos != null) oos.close(); } catch (Exception e) {}
        try { if (ois != null) ois.close(); } catch (Exception e) {}
        try { if (socket != null && !socket.isClosed()) socket.close(); } catch (Exception e) {}
        streamsInitialized = false;
        System.out.println("[StreamManager] Streams closed.");
    }
}

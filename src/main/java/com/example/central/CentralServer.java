package com.example.central;

import com.example.model.Meeting;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;

public class CentralServer {
    private Properties portMap;

    public CentralServer(String configFile) throws IOException {
        portMap = new Properties();
        try (InputStream in = new FileInputStream(configFile)) {
            portMap.load(in);
        }
    }

    public void start(int listenPort) throws IOException {
        ServerSocket server = new ServerSocket(listenPort);
        System.out.println("Central listening on " + listenPort);
        while (true) {
            Socket client = server.accept();
            new Thread(() -> handleClient(client)).start();
        }
    }

    private void handleClient(Socket sock) {
        try (var in = new java.io.ObjectInputStream(sock.getInputStream())) {
            Meeting meeting = (Meeting) in.readObject();
            dispatch(meeting);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void dispatch(Meeting meeting) {
        // Notificar a cada participante y al organizador
        meeting.getParticipants().forEach(name -> notifyEmployee(meeting, name));
        notifyEmployee(meeting, meeting.getOrganizer());
    }

    private void notifyEmployee(Meeting meeting, String name) {
        String portStr = portMap.getProperty(name);
        if (portStr == null) return;

        int port = Integer.parseInt(portStr);
        // usar el nombre de servicio Docker como host
        String host = name.toLowerCase();

        try (Socket sock = new Socket(host, port);
             ObjectOutputStream out = new ObjectOutputStream(sock.getOutputStream())) {
            out.writeObject(meeting);
        } catch (IOException e) {
            System.err.printf("Error notifying %s@%s:%d – %s%n", name, host, port, e.getMessage());
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: CentralServer <config.properties> <port>");
            System.exit(1);
        }
        String configFile = args[0];
        int port = Integer.parseInt(args[1]);

        CentralServer central = new CentralServer(configFile);
        central.start(port);
    }
}

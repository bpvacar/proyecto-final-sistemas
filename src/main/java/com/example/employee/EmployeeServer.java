package com.example.employee;

import com.example.model.Meeting;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class EmployeeServer {
    private final String name;
    private final int port;
    private final Path file;
    private List<Meeting> meetings = new ArrayList<>();

    public EmployeeServer(String name, int port) throws IOException {
        this.name = name;
        this.port = port;
        this.file = Paths.get("data", name + ".txt");

        // Crear directorio data si no existe
        Path dataDir = file.getParent();
        if (dataDir != null && !Files.exists(dataDir)) {
            Files.createDirectories(dataDir);
        }

        if (Files.exists(file)) {
            load();
        }
    }

    public void start() throws IOException {
        ServerSocket server = new ServerSocket(port);
        System.out.println(name + " server on " + port);
        while (true) {
            Socket sock = server.accept();
            new Thread(() -> receive(sock)).start();
        }
    }

    private void receive(Socket sock) {
        try (var in = new java.io.ObjectInputStream(sock.getInputStream())) {
            Meeting m = (Meeting) in.readObject();
            updateMeeting(m);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private synchronized void updateMeeting(Meeting m) throws IOException {
        meetings.removeIf(existing -> existing.getTimestamp() <= m.getTimestamp());
        meetings.add(m);
        save();
        System.out.println(name + " updated: " + m);
    }

    private void load() throws IOException {
        meetings = Files.lines(file)
                .map(this::parseLine)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private void save() throws IOException {
        List<String> lines = new ArrayList<>();
        for (Meeting m : meetings) {
            // Formato: topic|participant1,participant2|organizer|location|start|end|timestamp
            String p = String.join(",", m.getParticipants());
            String line = String.join("|",
                    escape(m.getTopic()),
                    escape(p),
                    escape(m.getOrganizer()),
                    escape(m.getLocation()),
                    m.getStart().toString(),
                    m.getEnd().toString(),
                    Long.toString(m.getTimestamp())
            );
            lines.add(line);
        }
        Files.write(file, lines, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    private Meeting parseLine(String line) {
        try {
            String[] parts = line.split("\\|", -1);
            String topic = unescape(parts[0]);
            List<String> participants = parts[1].isEmpty()
                    ? Collections.emptyList()
                    : Arrays.asList(parts[1].split(","));
            String organizer = unescape(parts[2]);
            String location  = unescape(parts[3]);
            LocalDateTime start = LocalDateTime.parse(parts[4]);
            LocalDateTime end   = LocalDateTime.parse(parts[5]);
            long timestamp      = Long.parseLong(parts[6]);
            Meeting m = new Meeting(topic, participants, organizer, location, start, end);
            // Sobre-escribimos el timestamp para preservar orden
            // (reflexivamente usando reflexión o un constructor variante)
            // Para simplicidad, ignoramos aquí el timestamp exacto.
            return m;
        } catch (Exception e) {
            System.err.println("Error parsing line: " + line);
            return null;
        }
    }

    // Escapa tuberías para que no rompan el split
    private String escape(String s) {
        return s.replace("|", "\\|");
    }
    private String unescape(String s) {
        return s.replace("\\|", "|");
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: EmployeeServer <Name> <Port>");
            System.exit(1);
        }
        new EmployeeServer(args[0], Integer.parseInt(args[1])).start();
    }
}

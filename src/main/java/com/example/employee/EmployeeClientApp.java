package com.example.employee;

import com.example.model.Meeting;

import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class EmployeeClientApp {
    private final String name;
    private final String centralHost;
    private final int centralPort;
    private final Scanner sc = new Scanner(System.in);
    // Lista de reuniones creadas en esta sesión
    private final List<Meeting> localMeetings = new ArrayList<>();

    public EmployeeClientApp(String name, String host, int port) {
        this.name = name;
        this.centralHost = host;
        this.centralPort = port;
    }

    public void run() {
        while (true) {
            System.out.println("\n1) Crear reunión  2) Modificar reunión  3) Salir");
            String opt = sc.nextLine().trim();
            switch (opt) {
                case "1":
                    createMeeting();
                    break;
                case "2":
                    modifyMeeting();
                    break;
                case "3":
                    return;
                default:
                    System.out.println("Opción inválida.");
            }
        }
    }

    private void createMeeting() {
        Meeting m = promptForMeeting(null);
        localMeetings.add(m);
        sendToCentral(m);
    }

    private void modifyMeeting() {
        if (localMeetings.isEmpty()) {
            System.out.println("No hay reuniones para modificar.");
            return;
        }
        // Mostrar índice y resumen de cada reunión
        for (int i = 0; i < localMeetings.size(); i++) {
            Meeting m = localMeetings.get(i);
            System.out.printf("%d) %s [%s -> %s]%n",
                    i + 1,
                    m.getTopic(),
                    m.getStart(),
                    m.getEnd());
        }
        System.out.print("Selecciona número de reunión a modificar: ");
        int idx;
        try {
            idx = Integer.parseInt(sc.nextLine().trim()) - 1;
            if (idx < 0 || idx >= localMeetings.size()) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            System.out.println("Índice inválido.");
            return;
        }
        // Pedir nuevos valores, pasando el Meeting original
        Meeting original = localMeetings.get(idx);
        Meeting modified = promptForMeeting(original);
        // Reemplazar en la lista y reenviar
        localMeetings.set(idx, modified);
        sendToCentral(modified);
    }

    /**
     * Si `base` es null, crea uno nuevo; si no, lo usa para mostrar valores por defecto
     */
    private Meeting promptForMeeting(Meeting base) {
        String topic, loc;
        List<String> parts;
        LocalDateTime start, end;

        // Tema
        System.out.print("Tema"
                + (base != null ? " [" + base.getTopic() + "]" : "")
                + ": ");
        topic = sc.nextLine().trim();
        if (topic.isEmpty() && base != null) topic = base.getTopic();

        // Participantes
        System.out.print("Participantes (coma)"
                + (base != null ? " " + base.getParticipants() : "")
                + ": ");
        String line = sc.nextLine();
        if (line.isBlank() && base != null) {
            parts = base.getParticipants();
        } else {
            parts = Arrays.stream(line.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
        }

        // Lugar
        System.out.print("Lugar"
                + (base != null ? " [" + base.getLocation() + "]" : "")
                + ": ");
        loc = sc.nextLine().trim();
        if (loc.isEmpty() && base != null) loc = base.getLocation();

        // Inicio
        while (true) {
            System.out.print("Inicio (YYYY-MM-DDTHH:MM)"
                    + (base != null ? " [" + base.getStart() + "]" : "")
                    + ": ");
            String sin = sc.nextLine().trim();
            if (sin.isEmpty() && base != null) {
                start = base.getStart();
                break;
            }
            try {
                start = LocalDateTime.parse(sin);
                break;
            } catch (Exception e) {
                System.out.println("Formato inválido, intenta de nuevo.");
            }
        }

        // Fin
        while (true) {
            System.out.print("Fin (YYYY-MM-DDTHH:MM)"
                    + (base != null ? " [" + base.getEnd() + "]" : "")
                    + ": ");
            String fin = sc.nextLine().trim();
            if (fin.isEmpty() && base != null) {
                end = base.getEnd();
                break;
            }
            try {
                end = LocalDateTime.parse(fin);
                break;
            } catch (Exception e) {
                System.out.println("Formato inválido, intenta de nuevo.");
            }
        }

        // El organizador es siempre este cliente
        return new Meeting(topic, parts, name, loc, start, end);
    }

    private void sendToCentral(Meeting m) {
        try (Socket sock = new Socket(centralHost, centralPort);
             ObjectOutputStream out = new ObjectOutputStream(sock.getOutputStream())) {
            out.writeObject(m);
            System.out.println("Reunión enviada al central.");
        } catch (Exception e) {
            System.err.println("Error enviando: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        if (args.length < 3) {
            System.err.println("Usage: EmployeeClientApp <Name> <CentralHost> <CentralPort>");
            System.exit(1);
        }
        new EmployeeClientApp(
                args[0],
                args[1],
                Integer.parseInt(args[2])
        ).run();
    }
}

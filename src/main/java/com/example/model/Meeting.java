package com.example.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

public class Meeting implements Serializable {
    private static final long serialVersionUID = 1L;
    private String topic;
    private List<String> participants;
    private String organizer;
    private String location;
    private LocalDateTime start;
    private LocalDateTime end;
    private long timestamp;

    public Meeting(String topic, List<String> participants, String organizer,
                   String location, LocalDateTime start, LocalDateTime end) {
        this.topic = topic;
        this.participants = participants;
        this.organizer = organizer;
        this.location = location;
        this.start = start;
        this.end = end;
        this.timestamp = System.currentTimeMillis();
    }

    public String getTopic() { return topic; }
    public List<String> getParticipants() { return participants; }
    public String getOrganizer() { return organizer; }
    public String getLocation() { return location; }
    public LocalDateTime getStart() { return start; }
    public LocalDateTime getEnd() { return end; }
    public long getTimestamp() { return timestamp; }

    @Override
    public String toString() {
        return String.format("Meeting[topic=%s, organizer=%s, start=%s, end=%s]",
                topic, organizer, start, end);
    }
}
package com.calendar.dto;

import com.calendar.domain.Meeting;
import com.calendar.domain.MeetingStatus;
import java.time.LocalDateTime;
import java.util.List;

public class MeetingResponse {

    private Long id;
    private String title;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private MeetingStatus status;
    private List<String> participantNames;

    public static MeetingResponse from(Meeting meeting) {
        MeetingResponse r = new MeetingResponse();
        r.id = meeting.getId();
        r.title = meeting.getTitle();
        r.startTime = meeting.getStartTime();
        r.endTime = meeting.getEndTime();
        r.status = meeting.getStatus();
        r.participantNames = meeting.getParticipants().stream()
            .map(u -> u.getDisplayName())
            .toList();
        return r;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public MeetingStatus getStatus() { return status; }
    public List<String> getParticipantNames() { return participantNames; }
}

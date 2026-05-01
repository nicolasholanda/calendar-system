package com.calendar.service;

import com.calendar.domain.Meeting;
import com.calendar.domain.MeetingStatus;
import com.calendar.domain.User;
import com.calendar.repository.MeetingRepository;
import com.calendar.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class MeetingService {

    private final MeetingRepository meetingRepository;
    private final UserRepository userRepository;

    public MeetingService(MeetingRepository meetingRepository, UserRepository userRepository) {
        this.meetingRepository = meetingRepository;
        this.userRepository = userRepository;
    }

    public Meeting book(String title, LocalDateTime startTime, LocalDateTime endTime, List<String> participantUsernames) {
        if (!endTime.isAfter(startTime)) {
            throw new IllegalArgumentException("End time must be after start time");
        }
        List<User> participants = userRepository.findByUsernameIn(participantUsernames);
        if (participants.size() != participantUsernames.size()) {
            throw new IllegalArgumentException("One or more users not found");
        }
        Meeting meeting = new Meeting();
        meeting.setTitle(title);
        meeting.setStartTime(startTime);
        meeting.setEndTime(endTime);
        meeting.setParticipants(participants);
        return meetingRepository.save(meeting);
    }

    public void cancel(Long meetingId) {
        Meeting meeting = meetingRepository.findById(meetingId)
            .orElseThrow(() -> new IllegalArgumentException("Meeting not found: " + meetingId));
        if (meeting.getStatus() == MeetingStatus.CANCELLED) {
            throw new IllegalStateException("Meeting is already cancelled");
        }
        meeting.setStatus(MeetingStatus.CANCELLED);
        meetingRepository.save(meeting);
    }

    @Transactional(readOnly = true)
    public List<Meeting> listScheduled() {
        return meetingRepository.findByStatusOrderByStartTimeAsc(MeetingStatus.SCHEDULED);
    }

    @Transactional(readOnly = true)
    public List<User> listAllUsers() {
        return userRepository.findAll();
    }
}

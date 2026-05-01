package com.calendar.service;

import com.calendar.domain.Meeting;
import com.calendar.domain.User;
import com.calendar.repository.MeetingRepository;
import com.calendar.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class TimeSlotSuggestionService {

    private static final LocalTime WORK_START = LocalTime.of(8, 0);
    private static final LocalTime WORK_END = LocalTime.of(18, 0);
    private static final int SLOT_DURATION_MINUTES = 60;
    private static final int DAYS_TO_SCAN = 7;

    private final MeetingRepository meetingRepository;
    private final UserRepository userRepository;

    public TimeSlotSuggestionService(MeetingRepository meetingRepository, UserRepository userRepository) {
        this.meetingRepository = meetingRepository;
        this.userRepository = userRepository;
    }

    public List<LocalDateTime[]> suggest(String usernameA, String usernameB, LocalDate fromDate) {
        User userA = userRepository.findByUsername(usernameA)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + usernameA));
        User userB = userRepository.findByUsername(usernameB)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + usernameB));

        LocalDateTime rangeStart = fromDate.atTime(WORK_START);
        LocalDateTime rangeEnd = fromDate.plusDays(DAYS_TO_SCAN).atTime(WORK_END);

        List<Meeting> busyMeetings = meetingRepository.findScheduledMeetingsForUsersInRange(
            List.of(userA.getId(), userB.getId()), rangeStart, rangeEnd
        );

        List<LocalDateTime[]> suggestions = new ArrayList<>();
        LocalDate day = fromDate;

        while (!day.isAfter(fromDate.plusDays(DAYS_TO_SCAN - 1)) && suggestions.size() < 5) {
            LocalTime cursor = WORK_START;
            while (!cursor.plusMinutes(SLOT_DURATION_MINUTES).isAfter(WORK_END) && suggestions.size() < 5) {
                LocalDateTime slotStart = day.atTime(cursor);
                LocalDateTime slotEnd = slotStart.plusMinutes(SLOT_DURATION_MINUTES);
                if (isSlotFree(busyMeetings, slotStart, slotEnd)) {
                    suggestions.add(new LocalDateTime[]{slotStart, slotEnd});
                }
                cursor = cursor.plusMinutes(SLOT_DURATION_MINUTES);
            }
            day = day.plusDays(1);
        }

        return suggestions;
    }

    private boolean isSlotFree(List<Meeting> meetings, LocalDateTime start, LocalDateTime end) {
        for (Meeting m : meetings) {
            if (m.getStartTime().isBefore(end) && m.getEndTime().isAfter(start)) {
                return false;
            }
        }
        return true;
    }
}

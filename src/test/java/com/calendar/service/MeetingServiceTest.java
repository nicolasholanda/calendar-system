package com.calendar.service;

import com.calendar.domain.Meeting;
import com.calendar.domain.MeetingStatus;
import com.calendar.domain.User;
import com.calendar.repository.MeetingRepository;
import com.calendar.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MeetingServiceTest {

    @Mock
    private MeetingRepository meetingRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MeetingService meetingService;

    private User alice;
    private User bob;

    @BeforeEach
    void setUp() {
        alice = new User();
        alice.setId(1L);
        alice.setUsername("alice");
        alice.setDisplayName("Alice Johnson");

        bob = new User();
        bob.setId(2L);
        bob.setUsername("bob");
        bob.setDisplayName("Bob Smith");
    }

    @Test
    void book_savesAndReturnsMeeting() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = start.plusHours(1);

        when(userRepository.findByUsernameIn(List.of("alice", "bob"))).thenReturn(List.of(alice, bob));
        when(meetingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Meeting result = meetingService.book("Standup", start, end, List.of("alice", "bob"));

        assertThat(result.getTitle()).isEqualTo("Standup");
        assertThat(result.getStatus()).isEqualTo(MeetingStatus.SCHEDULED);
        assertThat(result.getParticipants()).containsExactlyInAnyOrder(alice, bob);
    }

    @Test
    void book_throwsWhenEndBeforeStart() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = start.minusHours(1);

        assertThatThrownBy(() -> meetingService.book("Bad", start, end, List.of("alice")))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("End time must be after start time");
    }

    @Test
    void book_throwsWhenUserNotFound() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        when(userRepository.findByUsernameIn(List.of("ghost"))).thenReturn(List.of());

        assertThatThrownBy(() -> meetingService.book("Meeting", start, start.plusHours(1), List.of("ghost")))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("One or more users not found");
    }

    @Test
    void cancel_setsMeetingCancelled() {
        Meeting meeting = new Meeting();
        meeting.setId(1L);
        meeting.setStatus(MeetingStatus.SCHEDULED);

        when(meetingRepository.findById(1L)).thenReturn(Optional.of(meeting));
        when(meetingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        meetingService.cancel(1L);

        assertThat(meeting.getStatus()).isEqualTo(MeetingStatus.CANCELLED);
    }

    @Test
    void cancel_throwsWhenAlreadyCancelled() {
        Meeting meeting = new Meeting();
        meeting.setId(1L);
        meeting.setStatus(MeetingStatus.CANCELLED);

        when(meetingRepository.findById(1L)).thenReturn(Optional.of(meeting));

        assertThatThrownBy(() -> meetingService.cancel(1L))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("already cancelled");
    }

    @Test
    void cancel_throwsWhenMeetingNotFound() {
        when(meetingRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> meetingService.cancel(99L))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Meeting not found");
    }
}

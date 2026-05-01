package com.calendar.service;

import com.calendar.domain.Meeting;
import com.calendar.domain.User;
import com.calendar.repository.MeetingRepository;
import com.calendar.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TimeSlotSuggestionServiceTest {

    @Mock
    private MeetingRepository meetingRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TimeSlotSuggestionService suggestionService;

    private User alice;
    private User bob;
    private LocalDate fromDate;

    @BeforeEach
    void setUp() {
        alice = new User();
        alice.setId(1L);
        alice.setUsername("alice");

        bob = new User();
        bob.setId(2L);
        bob.setUsername("bob");

        fromDate = LocalDate.now().plusDays(1);
    }

    @Test
    void suggest_returnsUpToFiveSlots_whenNoConflicts() {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(alice));
        when(userRepository.findByUsername("bob")).thenReturn(Optional.of(bob));
        when(meetingRepository.findScheduledMeetingsForUsersInRange(any(), any(), any()))
            .thenReturn(List.of());

        List<LocalDateTime[]> slots = suggestionService.suggest("alice", "bob", fromDate);

        assertThat(slots).hasSize(5);
        for (LocalDateTime[] slot : slots) {
            assertThat(slot[1]).isEqualTo(slot[0].plusHours(1));
            assertThat(slot[0].toLocalTime()).isGreaterThanOrEqualTo(LocalTime.of(8, 0));
            assertThat(slot[1].toLocalTime()).isLessThanOrEqualTo(LocalTime.of(18, 0));
        }
    }

    @Test
    void suggest_excludesBusySlots() {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(alice));
        when(userRepository.findByUsername("bob")).thenReturn(Optional.of(bob));

        Meeting busy = new Meeting();
        busy.setStartTime(fromDate.atTime(8, 0));
        busy.setEndTime(fromDate.atTime(9, 0));

        when(meetingRepository.findScheduledMeetingsForUsersInRange(any(), any(), any()))
            .thenReturn(List.of(busy));

        List<LocalDateTime[]> slots = suggestionService.suggest("alice", "bob", fromDate);

        assertThat(slots).isNotEmpty();
        for (LocalDateTime[] slot : slots) {
            boolean overlaps = slot[0].isBefore(busy.getEndTime()) && slot[1].isAfter(busy.getStartTime());
            assertThat(overlaps).isFalse();
        }
    }

    @Test
    void suggest_throwsWhenUserNotFound() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> suggestionService.suggest("ghost", "bob", fromDate))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("User not found");
    }
}

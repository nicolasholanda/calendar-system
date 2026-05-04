package com.calendar.controller;

import com.calendar.domain.Meeting;
import com.calendar.service.MeetingService;
import com.calendar.service.TimeSlotSuggestionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MeetingController.class)
class MeetingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MeetingService meetingService;

    @MockitoBean
    private TimeSlotSuggestionService suggestionService;

    @Test
    void list_returnsListView() throws Exception {
        when(meetingService.listScheduled()).thenReturn(List.of());

        mockMvc.perform(get("/meetings"))
            .andExpect(status().isOk())
            .andExpect(view().name("list"))
            .andExpect(model().attributeExists("meetings"));
    }

    @Test
    void bookForm_returnsBookView() throws Exception {
        when(meetingService.listAllUsers()).thenReturn(List.of());

        mockMvc.perform(get("/meetings/book"))
            .andExpect(status().isOk())
            .andExpect(view().name("book"))
            .andExpect(model().attributeExists("meetingRequest", "users"));
    }

    @Test
    void book_redirectsOnSuccess() throws Exception {
        when(meetingService.listAllUsers()).thenReturn(List.of());
        when(meetingService.book(any(), any(), any(), any())).thenReturn(new Meeting());

        mockMvc.perform(post("/meetings/book")
                .param("title", "Standup")
                .param("startTime", "2027-01-10T09:00")
                .param("endTime", "2027-01-10T10:00")
                .param("participantUsernames", "alice"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/meetings"))
            .andExpect(flash().attribute("success", "Meeting booked successfully."));
    }

    @Test
    void book_returnsBookViewOnValidationError() throws Exception {
        when(meetingService.listAllUsers()).thenReturn(List.of());

        mockMvc.perform(post("/meetings/book")
                .param("title", "")
                .param("startTime", "2027-01-10T09:00")
                .param("endTime", "2027-01-10T10:00")
                .param("participantUsernames", "alice"))
            .andExpect(status().isOk())
            .andExpect(view().name("book"));
    }

    @Test
    void book_returnsBookViewOnServiceException() throws Exception {
        when(meetingService.listAllUsers()).thenReturn(List.of());
        when(meetingService.book(any(), any(), any(), any()))
            .thenThrow(new IllegalArgumentException("End time must be after start time"));

        mockMvc.perform(post("/meetings/book")
                .param("title", "Standup")
                .param("startTime", "2027-01-10T09:00")
                .param("endTime", "2027-01-10T10:00")
                .param("participantUsernames", "alice"))
            .andExpect(status().isOk())
            .andExpect(view().name("book"))
            .andExpect(model().attribute("error", "End time must be after start time"));
    }

    @Test
    void cancel_redirectsToMeetings() throws Exception {
        mockMvc.perform(post("/meetings/1/cancel"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/meetings"))
            .andExpect(flash().attribute("success", "Meeting cancelled."));
    }

    @Test
    void suggestForm_returnsSuggestView() throws Exception {
        when(meetingService.listAllUsers()).thenReturn(List.of());

        mockMvc.perform(get("/meetings/suggest"))
            .andExpect(status().isOk())
            .andExpect(view().name("suggest"))
            .andExpect(model().attributeExists("suggestionRequest", "users"));
    }

    @Test
    void suggest_addsSlots() throws Exception {
        LocalDateTime slot = LocalDateTime.of(2027, 1, 10, 9, 0);
        when(meetingService.listAllUsers()).thenReturn(List.of());
        when(suggestionService.suggest(any(), any(), any()))
            .thenReturn(List.of(new LocalDateTime[]{slot, slot.plusHours(1)}));

        mockMvc.perform(post("/meetings/suggest")
                .param("usernameA", "alice")
                .param("usernameB", "bob")
                .param("fromDate", "2027-01-10"))
            .andExpect(status().isOk())
            .andExpect(view().name("suggest"))
            .andExpect(model().attributeExists("slots"));
    }

    @Test
    void suggest_returnsSuggestViewOnValidationError() throws Exception {
        when(meetingService.listAllUsers()).thenReturn(List.of());

        mockMvc.perform(post("/meetings/suggest")
                .param("usernameA", "")
                .param("usernameB", "bob")
                .param("fromDate", "2027-01-10"))
            .andExpect(status().isOk())
            .andExpect(view().name("suggest"));
    }

    @Test
    void suggest_returnsErrorOnServiceException() throws Exception {
        when(meetingService.listAllUsers()).thenReturn(List.of());
        when(suggestionService.suggest(any(), any(), any()))
            .thenThrow(new IllegalArgumentException("User not found"));

        mockMvc.perform(post("/meetings/suggest")
                .param("usernameA", "ghost")
                .param("usernameB", "bob")
                .param("fromDate", "2027-01-10"))
            .andExpect(status().isOk())
            .andExpect(view().name("suggest"))
            .andExpect(model().attribute("error", "User not found"));
    }
}

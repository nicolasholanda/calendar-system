package com.calendar.controller;

import com.calendar.dto.MeetingRequest;
import com.calendar.dto.MeetingResponse;
import com.calendar.dto.SuggestionRequest;
import com.calendar.service.MeetingService;
import com.calendar.service.TimeSlotSuggestionService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/meetings")
public class MeetingController {

    private final MeetingService meetingService;
    private final TimeSlotSuggestionService suggestionService;

    public MeetingController(MeetingService meetingService, TimeSlotSuggestionService suggestionService) {
        this.meetingService = meetingService;
        this.suggestionService = suggestionService;
    }

    @GetMapping
    public String list(Model model) {
        List<MeetingResponse> meetings = meetingService.listScheduled().stream()
            .map(MeetingResponse::from)
            .toList();
        model.addAttribute("meetings", meetings);
        return "list";
    }

    @GetMapping("/book")
    public String bookForm(Model model) {
        model.addAttribute("meetingRequest", new MeetingRequest());
        model.addAttribute("users", meetingService.listAllUsers());
        return "book";
    }

    @PostMapping("/book")
    public String book(@Valid @ModelAttribute MeetingRequest meetingRequest,
                       BindingResult result,
                       Model model,
                       RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("users", meetingService.listAllUsers());
            return "book";
        }
        try {
            meetingService.book(
                meetingRequest.getTitle(),
                meetingRequest.getStartTime(),
                meetingRequest.getEndTime(),
                meetingRequest.getParticipantUsernames()
            );
            redirectAttributes.addFlashAttribute("success", "Meeting booked successfully.");
            return "redirect:/meetings";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("users", meetingService.listAllUsers());
            return "book";
        }
    }

    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        meetingService.cancel(id);
        redirectAttributes.addFlashAttribute("success", "Meeting cancelled.");
        return "redirect:/meetings";
    }

    @GetMapping("/suggest")
    public String suggestForm(Model model) {
        model.addAttribute("suggestionRequest", new SuggestionRequest());
        model.addAttribute("users", meetingService.listAllUsers());
        return "suggest";
    }

    @PostMapping("/suggest")
    public String suggest(@Valid @ModelAttribute SuggestionRequest suggestionRequest,
                          BindingResult result,
                          Model model) {
        model.addAttribute("users", meetingService.listAllUsers());
        if (result.hasErrors()) {
            return "suggest";
        }
        try {
            List<LocalDateTime[]> slots = suggestionService.suggest(
                suggestionRequest.getUsernameA(),
                suggestionRequest.getUsernameB(),
                suggestionRequest.getFromDate() != null ? suggestionRequest.getFromDate() : LocalDate.now()
            );
            model.addAttribute("slots", slots);
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
        }
        return "suggest";
    }
}

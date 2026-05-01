package com.calendar.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;

public class SuggestionRequest {

    @NotBlank
    private String usernameA;

    @NotBlank
    private String usernameB;

    @NotNull
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate fromDate;

    public String getUsernameA() { return usernameA; }
    public void setUsernameA(String usernameA) { this.usernameA = usernameA; }

    public String getUsernameB() { return usernameB; }
    public void setUsernameB(String usernameB) { this.usernameB = usernameB; }

    public LocalDate getFromDate() { return fromDate; }
    public void setFromDate(LocalDate fromDate) { this.fromDate = fromDate; }
}

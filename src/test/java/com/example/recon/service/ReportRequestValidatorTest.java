package com.example.recon.service;

import com.example.recon.dto.SearchCriteria;
import com.example.recon.exception.InvalidReportRequestException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertThrows;

class ReportRequestValidatorTest {

    private final ReportRequestValidator validator = new ReportRequestValidator();

    @Test
    void validate_requiresFromDate() {
        SearchCriteria c = new SearchCriteria(null, LocalDate.of(2026, 1, 2), null, null);
        assertThrows(InvalidReportRequestException.class, () -> validator.validate(c));
    }

    @Test
    void validate_requiresToDate() {
        SearchCriteria c = new SearchCriteria(LocalDate.of(2026, 1, 1), null, null, null);
        assertThrows(InvalidReportRequestException.class, () -> validator.validate(c));
    }

    @Test
    void validate_rejectsFromAfterTo() {
        SearchCriteria c = new SearchCriteria(LocalDate.of(2026, 1, 3), LocalDate.of(2026, 1, 2), null, null);
        assertThrows(InvalidReportRequestException.class, () -> validator.validate(c));
    }

    @Test
    void validate_rejectsFromEqualToTo() {
        SearchCriteria c = new SearchCriteria(LocalDate.of(2026, 1, 2), LocalDate.of(2026, 1, 2), null, null);
        assertThrows(InvalidReportRequestException.class, () -> validator.validate(c));
    }
}


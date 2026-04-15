package com.example.recon.service;

import com.example.recon.dto.SearchCriteria;
import com.example.recon.exception.InvalidReportRequestException;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class ReportRequestValidator {

    /**
     * Assumption (aligned with requirement pseudo-SQL):
     * - {@code toDate} is treated as an exclusive boundary (DateCreated &lt; toDate).
     * - For a non-empty window, {@code toDate} must be strictly after {@code fromDate}
     *   (e.g. one calendar day in UTC: fromDate=2026-01-02, toDate=2026-01-03).
     */
    public void validate(SearchCriteria criteria) {
        if (criteria == null) {
            throw new InvalidReportRequestException("criteria must not be null");
        }
        LocalDate from = criteria.getFromDate();
        LocalDate to = criteria.getToDate();
        if (from == null) {
            throw new InvalidReportRequestException("fromDate is required");
        }
        if (to == null) {
            throw new InvalidReportRequestException("toDate is required");
        }
        if (from.isAfter(to)) {
            throw new InvalidReportRequestException("fromDate must be <= toDate");
        }
        if (!to.isAfter(from)) {
            throw new InvalidReportRequestException(
                    "toDate must be after fromDate (exclusive end); for one UTC day use toDate=fromDate+1 day");
        }
    }
}


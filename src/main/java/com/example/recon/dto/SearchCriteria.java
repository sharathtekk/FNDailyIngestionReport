package com.example.recon.dto;

import java.time.LocalDate;

public class SearchCriteria {
    private final LocalDate fromDate;
    private final LocalDate toDate;
    private final String formType;
    private final String source;

    public SearchCriteria(LocalDate fromDate, LocalDate toDate, String formType, String source) {
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.formType = formType;
        this.source = source;
    }

    public LocalDate getFromDate() {
        return fromDate;
    }

    public LocalDate getToDate() {
        return toDate;
    }

    public String getFormType() {
        return formType;
    }

    public String getSource() {
        return source;
    }
}


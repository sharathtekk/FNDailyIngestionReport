package com.example.recon.dto;

import java.time.LocalDate;

public class ReportRow {
    private final LocalDate dateCreated;
    private final String formType;
    private final String source;
    private final long count;

    public ReportRow(LocalDate dateCreated, String formType, String source, long count) {
        this.dateCreated = dateCreated;
        this.formType = formType;
        this.source = source;
        this.count = count;
    }

    public LocalDate getDateCreated() {
        return dateCreated;
    }

    public String getFormType() {
        return formType;
    }

    public String getSource() {
        return source;
    }

    public long getCount() {
        return count;
    }
}


package com.example.recon.dto;

import java.time.LocalDate;

public class DocumentRecord {
    private final LocalDate dateCreated;
    private final String formType;
    private final String source;

    public DocumentRecord(LocalDate dateCreated, String formType, String source) {
        this.dateCreated = dateCreated;
        this.formType = formType;
        this.source = source;
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
}


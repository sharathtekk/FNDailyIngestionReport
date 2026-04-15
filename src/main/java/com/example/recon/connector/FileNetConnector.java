package com.example.recon.connector;

import com.example.recon.dto.DocumentRecord;
import com.example.recon.dto.SearchCriteria;

import java.util.List;

/**
 * Abstraction over IBM FileNet P8 access.
 *
 * Assumption: A real implementation would be provided in another module/repo
 * that depends on the vendor client libraries. This project intentionally does
 * not reference IBM APIs (per requirement).
 */
public interface FileNetConnector {
    List<DocumentRecord> fetchDocuments(SearchCriteria criteria);
}


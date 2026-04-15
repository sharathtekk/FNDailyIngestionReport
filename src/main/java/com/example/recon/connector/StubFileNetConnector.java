package com.example.recon.connector;

import com.example.recon.dto.DocumentRecord;
import com.example.recon.dto.SearchCriteria;
import com.example.recon.exception.FileNetConnectionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Stub connector for local runs and unit/integration testing without FileNet.
 *
 * Assumptions:
 * - This stub returns synthetic data and does not attempt any network calls.
 * - A real connector would translate {@link SearchCriteria} into a FileNet query
 *   and map results into {@link DocumentRecord}.
 */
@Component
@ConditionalOnProperty(prefix = "app.filenet", name = "enabled", havingValue = "false", matchIfMissing = true)
public class StubFileNetConnector implements FileNetConnector {

    private static final Logger log = LoggerFactory.getLogger(StubFileNetConnector.class);

    /**
     * Assumption: Controlled via environment for demo purposes. In production,
     * connection failures would come from underlying FileNet client calls.
     */
    private final boolean simulateConnectionFailure;

    public StubFileNetConnector() {
        this(Boolean.parseBoolean(System.getProperty("stub.filenet.failConnection", "false")));
    }

    StubFileNetConnector(boolean simulateConnectionFailure) {
        this.simulateConnectionFailure = simulateConnectionFailure;
    }

    @Override
    public List<DocumentRecord> fetchDocuments(SearchCriteria criteria) {
        if (simulateConnectionFailure) {
            throw new FileNetConnectionException("Stubbed FileNet connection failure");
        }

        // Minimal synthetic dataset; real implementation would query FileNet.
        if (criteria == null) {
            return Collections.emptyList();
        }

        List<DocumentRecord> all = new ArrayList<DocumentRecord>();
        LocalDate d0 = criteria.getFromDate();
        if (d0 != null) {
            all.add(new DocumentRecord(d0, "FT_A", "SRC_1"));
            all.add(new DocumentRecord(d0, "FT_A", "SRC_1"));
            all.add(new DocumentRecord(d0, "FT_A", "SRC_2"));
            all.add(new DocumentRecord(d0.plusDays(1), "FT_B", "SRC_1"));
        }

        List<DocumentRecord> filtered = new ArrayList<DocumentRecord>();
        for (DocumentRecord r : all) {
            if (r.getDateCreated() == null) {
                continue;
            }
            if (criteria.getFromDate() != null && r.getDateCreated().isBefore(criteria.getFromDate())) {
                continue;
            }
            if (criteria.getToDate() != null && !r.getDateCreated().isBefore(criteria.getToDate())) {
                continue;
            }
            if (criteria.getFormType() != null && !criteria.getFormType().equals(r.getFormType())) {
                continue;
            }
            if (criteria.getSource() != null && !criteria.getSource().equals(r.getSource())) {
                continue;
            }
            filtered.add(r);
        }

        log.debug("event=stub_filenet_fetch fetched={}", filtered.size());
        return filtered;
    }
}


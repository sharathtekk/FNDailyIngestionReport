package com.example.recon.service;

import com.example.recon.dto.DocumentRecord;
import com.example.recon.dto.ReportRow;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReconciliationServiceTest {

    private final ReconciliationService svc = new ReconciliationService();

    @Test
    void reconcile_groupsByDateFormTypeSource() {
        LocalDate d = LocalDate.of(2026, 1, 1);

        List<DocumentRecord> docs = Arrays.asList(
                new DocumentRecord(d, "A", "S1"),
                new DocumentRecord(d, "A", "S1"),
                new DocumentRecord(d, "A", "S2"),
                new DocumentRecord(d.plusDays(1), "A", "S1")
        );

        List<ReportRow> rows = svc.reconcile(docs);
        assertEquals(3, rows.size());

        // Sorted: date, formType, source
        assertEquals(d, rows.get(0).getDateCreated());
        assertEquals("A", rows.get(0).getFormType());
        assertEquals("S1", rows.get(0).getSource());
        assertEquals(2L, rows.get(0).getCount());

        assertEquals(d, rows.get(1).getDateCreated());
        assertEquals("A", rows.get(1).getFormType());
        assertEquals("S2", rows.get(1).getSource());
        assertEquals(1L, rows.get(1).getCount());

        assertEquals(d.plusDays(1), rows.get(2).getDateCreated());
        assertEquals("A", rows.get(2).getFormType());
        assertEquals("S1", rows.get(2).getSource());
        assertEquals(1L, rows.get(2).getCount());
    }
}


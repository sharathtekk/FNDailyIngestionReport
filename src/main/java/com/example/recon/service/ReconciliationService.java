package com.example.recon.service;

import com.example.recon.dto.DocumentRecord;
import com.example.recon.dto.ReportRow;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class ReconciliationService {

    public List<ReportRow> reconcile(List<DocumentRecord> documents) {
        if (documents == null || documents.isEmpty()) {
            return Collections.emptyList();
        }

        Map<GroupKey, Long> counts = new HashMap<GroupKey, Long>();
        for (DocumentRecord doc : documents) {
            if (doc == null) {
                continue;
            }
            GroupKey key = new GroupKey(doc.getDateCreated(), doc.getFormType(), doc.getSource());
            Long current = counts.get(key);
            counts.put(key, current == null ? 1L : current + 1L);
        }

        List<ReportRow> rows = new ArrayList<ReportRow>(counts.size());
        for (Map.Entry<GroupKey, Long> e : counts.entrySet()) {
            GroupKey k = e.getKey();
            rows.add(new ReportRow(k.dateCreated, k.formType, k.source, e.getValue().longValue()));
        }

        Collections.sort(rows, new Comparator<ReportRow>() {
            @Override
            public int compare(ReportRow a, ReportRow b) {
                int c = nullSafe(a.getDateCreated()).compareTo(nullSafe(b.getDateCreated()));
                if (c != 0) {
                    return c;
                }
                c = nullSafe(a.getFormType()).compareTo(nullSafe(b.getFormType()));
                if (c != 0) {
                    return c;
                }
                return nullSafe(a.getSource()).compareTo(nullSafe(b.getSource()));
            }

            private LocalDate nullSafe(LocalDate d) {
                return d == null ? LocalDate.of(1970, 1, 1) : d;
            }

            private String nullSafe(String s) {
                return s == null ? "" : s;
            }
        });

        return rows;
    }

    private static final class GroupKey {
        private final LocalDate dateCreated;
        private final String formType;
        private final String source;

        private GroupKey(LocalDate dateCreated, String formType, String source) {
            this.dateCreated = dateCreated;
            this.formType = formType;
            this.source = source;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            GroupKey groupKey = (GroupKey) o;
            return Objects.equals(dateCreated, groupKey.dateCreated)
                    && Objects.equals(formType, groupKey.formType)
                    && Objects.equals(source, groupKey.source);
        }

        @Override
        public int hashCode() {
            return Objects.hash(dateCreated, formType, source);
        }
    }
}


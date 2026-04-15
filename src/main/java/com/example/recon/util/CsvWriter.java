package com.example.recon.util;

import com.example.recon.dto.ReportRow;
import com.example.recon.exception.ReportWriteException;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Component
public class CsvWriter {

    private static final String[] HEADER = new String[]{"DateCreated", "count", "formType", "source"};

    public void writeReport(Path path, List<ReportRow> rows) {
        if (path == null) {
            throw new ReportWriteException("CSV path must not be null");
        }
        try (Writer w = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(path), StandardCharsets.UTF_8))) {
            writeLine(w, HEADER[0], HEADER[1], HEADER[2], HEADER[3]);
            if (rows != null) {
                for (ReportRow r : rows) {
                    writeLine(w,
                            r.getDateCreated() == null ? "" : r.getDateCreated().toString(),
                            String.valueOf(r.getCount()),
                            nullToEmpty(r.getFormType()),
                            nullToEmpty(r.getSource()));
                }
            }
        } catch (IOException e) {
            throw new ReportWriteException("Failed to write CSV to path=" + path, e);
        }
    }

    public void writeLine(Writer w, String c1, String c2, String c3, String c4) throws IOException {
        w.write(escape(c1));
        w.write(',');
        w.write(escape(c2));
        w.write(',');
        w.write(escape(c3));
        w.write(',');
        w.write(escape(c4));
        w.write('\n');
    }

    /**
     * RFC 4180-style escaping:
     * - fields containing comma, quote, CR or LF are wrapped in quotes
     * - quotes are doubled
     */
    public String escape(String value) {
        if (value == null) {
            return "";
        }
        boolean needsQuotes = false;
        int len = value.length();
        for (int i = 0; i < len; i++) {
            char ch = value.charAt(i);
            if (ch == ',' || ch == '"' || ch == '\n' || ch == '\r') {
                needsQuotes = true;
                break;
            }
        }
        if (!needsQuotes) {
            return value;
        }
        StringBuilder sb = new StringBuilder(len + 2);
        sb.append('"');
        for (int i = 0; i < len; i++) {
            char ch = value.charAt(i);
            if (ch == '"') {
                sb.append("\"\"");
            } else {
                sb.append(ch);
            }
        }
        sb.append('"');
        return sb.toString();
    }

    private String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}


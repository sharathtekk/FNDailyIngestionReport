package com.example.recon.util;

import org.junit.jupiter.api.Test;

import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CsvWriterTest {

    private final CsvWriter csvWriter = new CsvWriter();

    @Test
    void escape_handlesCommaQuoteAndNewline() {
        assertEquals("simple", csvWriter.escape("simple"));
        assertEquals("\"a,b\"", csvWriter.escape("a,b"));
        assertEquals("\"a\"\"b\"", csvWriter.escape("a\"b"));
        assertEquals("\"a\nb\"", csvWriter.escape("a\nb"));
        assertEquals("", csvWriter.escape(null));
    }

    @Test
    void writeLine_writesEscapedCsv() throws Exception {
        StringWriter w = new StringWriter();
        csvWriter.writeLine(w, "2026-01-01", "2", "A,B", "S\"1");
        assertEquals("2026-01-01,2,\"A,B\",\"S\"\"1\"\n", w.toString());
    }
}


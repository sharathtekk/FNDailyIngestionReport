package com.example.recon.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FilenetSearchSqlDatesTest {

    @Test
    void utcStartOfDayLiteral_compactUtcMidnightZ() {
        assertEquals("20260102T000000Z", FilenetSearchSqlDates.utcStartOfDayLiteral(LocalDate.of(2026, 1, 2)));
    }
}

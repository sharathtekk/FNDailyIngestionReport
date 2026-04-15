package com.example.recon.util;

import java.time.LocalDate;

/**
 * FileNet Content Engine SearchSQL date/time literals.
 * <p>
 * CE SQL is not ANSI SQL: {@code DATE('yyyy-MM-dd')} and similar constructs are not valid and
 * commonly fail with parser errors (e.g. {@code EngineRuntimeException} on {@code DATE}).
 * </p>
 * <p>
 * Assumption (documented): report windows use <strong>UTC</strong> calendar-day boundaries.
 * Boundaries are expressed as ISO-8601 compact UTC literals at 00:00:00Z, which matches common
 * IBM examples and field comparisons on {@code DateTime} properties.
 * </p>
 * <p>
 * For an inclusive start and <strong>exclusive</strong> end (same as {@code >= from AND < to}):
 * use {@code utcStartOfDayLiteral(fromDate)} and {@code utcStartOfDayLiteral(toDate)} where
 * {@code toDate} is the day <em>after</em> the last day to include (e.g. one day: from=2026-01-02,
 * to=2026-01-03).
 * </p>
 */
public final class FilenetSearchSqlDates {

    private FilenetSearchSqlDates() {
    }

    /**
     * Returns a CE SearchSQL literal for midnight UTC on the given local calendar date, e.g.
     * {@code 20260102T000000Z}. Not quoted — matches common CE SQL samples.
     */
    public static String utcStartOfDayLiteral(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("date must not be null");
        }
        return String.format("%04d%02d%02dT000000Z",
                date.getYear(), date.getMonthValue(), date.getDayOfMonth());
    }
}

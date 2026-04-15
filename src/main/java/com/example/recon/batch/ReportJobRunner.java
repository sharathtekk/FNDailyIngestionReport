package com.example.recon.batch;

import com.example.recon.config.AppProperties;
import com.example.recon.dto.SearchCriteria;
import com.example.recon.exception.InvalidReportRequestException;
import com.example.recon.service.ReportService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Component
public class ReportJobRunner implements CommandLineRunner, ExitCodeGenerator {

    private static final Logger log = LoggerFactory.getLogger(ReportJobRunner.class);

    private final ApplicationContext context;
    private final AppProperties props;
    private final ReportService reportService;

    private volatile int exitCode = 0;

    public ReportJobRunner(ApplicationContext context, AppProperties props, ReportService reportService) {
        this.context = context;
        this.props = props;
        this.reportService = reportService;
    }

    @Override
    public void run(String... args) {
        UUID jobId = UUID.randomUUID();
        Instant start = Instant.now();

        DefaultApplicationArguments appArgs = new DefaultApplicationArguments(args);

        try {
            SearchCriteria criteria = parseCriteria(appArgs);
            log.info("event=job_start jobId={} startTime={} fromDate={} toDate={} formType={} source={} outputPath={}",
                    jobId, start, criteria.getFromDate(), criteria.getToDate(), criteria.getFormType(), criteria.getSource(),
                    props.getReport().getOutputPath());

            ReportService.ReportResult result = reportService.generateReport(jobId, criteria);

            Instant end = Instant.now();
            log.info("event=job_end jobId={} status=SUCCESS durationMs={} generatedFile={} totalRowsFetched={} reportRowCount={}",
                    jobId, Duration.between(start, end).toMillis(), result.getFinalPath(),
                    result.getTotalRowsFetched(), result.getReportRowCount());

            exitCode = 0;
        } catch (InvalidReportRequestException e) {
            exitCode = 1;
            log.error("event=job_end jobId={} status=FAILURE errorType=InvalidReportRequestException message=\"{}\"",
                    jobId, safe(e.getMessage()));
        } catch (RuntimeException e) {
            exitCode = 1;
            log.error("event=job_end jobId={} status=FAILURE errorType={} message=\"{}\"",
                    jobId, e.getClass().getSimpleName(), safe(e.getMessage()), e);
        } finally {
            int code = exitCode;
            // Ensure proper process exit for batch execution.
            org.springframework.boot.SpringApplication.exit(context, this);
            System.exit(code);
        }
    }

    @Override
    public int getExitCode() {
        return exitCode;
    }

    private SearchCriteria parseCriteria(DefaultApplicationArguments args) {
        LocalDate fromDate = parseRequiredDate(args, "fromDate");
        LocalDate toDate = parseRequiredDate(args, "toDate");
        String formType = parseOptional(args, "formType");
        String source = parseOptional(args, "source");
        return new SearchCriteria(fromDate, toDate, blankToNull(formType), blankToNull(source));
    }

    private LocalDate parseRequiredDate(DefaultApplicationArguments args, String key) {
        String v = firstOption(args, key);
        if (v == null || v.trim().isEmpty()) {
            throw new InvalidReportRequestException(key + " is required (format YYYY-MM-DD)");
        }
        try {
            return LocalDate.parse(v.trim());
        } catch (Exception e) {
            throw new InvalidReportRequestException(key + " must be ISO date YYYY-MM-DD");
        }
    }

    private String parseOptional(DefaultApplicationArguments args, String key) {
        return firstOption(args, key);
    }

    private String firstOption(DefaultApplicationArguments args, String key) {
        if (!args.containsOption(key)) {
            return null;
        }
        List<String> values = args.getOptionValues(key);
        if (values == null || values.isEmpty()) {
            return null;
        }
        return values.get(0);
    }

    private String blankToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private String safe(String s) {
        return s == null ? "" : s.replace("\"", "'");
    }
}


package com.example.recon.service;

import com.example.recon.config.AppProperties;
import com.example.recon.connector.FileNetConnector;
import com.example.recon.dto.DocumentRecord;
import com.example.recon.dto.ReportRow;
import com.example.recon.dto.SearchCriteria;
import com.example.recon.exception.FileNetQueryTimeoutException;
import com.example.recon.exception.ReportWriteException;
import com.example.recon.util.CsvWriter;
import com.example.recon.util.FileManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
public class ReportService {

    private static final Logger log = LoggerFactory.getLogger(ReportService.class);

    private final AppProperties props;
    private final FileNetConnector connector;
    private final ReportRequestValidator validator;
    private final ReconciliationService reconciliationService;
    private final CsvWriter csvWriter;
    private final FileManager fileManager;

    public ReportService(AppProperties props,
                         FileNetConnector connector,
                         ReportRequestValidator validator,
                         ReconciliationService reconciliationService,
                         CsvWriter csvWriter,
                         FileManager fileManager) {
        this.props = props;
        this.connector = connector;
        this.validator = validator;
        this.reconciliationService = reconciliationService;
        this.csvWriter = csvWriter;
        this.fileManager = fileManager;
    }

    public ReportResult generateReport(UUID jobId, SearchCriteria criteria) {
        validator.validate(criteria);

        Instant queryStart = Instant.now();
        log.info("event=filenet_query_start jobId={} fromDate={} toDate={} formType={} source={}",
                jobId, criteria.getFromDate(), criteria.getToDate(), criteria.getFormType(), criteria.getSource());

        List<DocumentRecord> docs = fetchWithTimeout(criteria, props.getTimeout().getFilenetSeconds());

        Instant queryEnd = Instant.now();
        log.info("event=filenet_query_end jobId={} durationMs={} totalRowsFetched={}",
                jobId, Duration.between(queryStart, queryEnd).toMillis(), docs.size());

        List<ReportRow> reportRows = reconciliationService.reconcile(docs);
        log.info("event=reconciliation_complete jobId={} reportRowCount={}", jobId, reportRows.size());

        String fileBaseName = fileManager.buildBaseFileName(
                props.getReport().getFilePrefix(),
                criteria.getFromDate(),
                criteria.getToDate(),
                jobId
        );

        Path outputDir = fileManager.resolveOutputDir(props.getReport().getOutputPath());
        Path finalFile = outputDir.resolve(fileBaseName + ".csv");
        Path tmpFile = outputDir.resolve(fileBaseName + ".tmp");

        try {
            fileManager.ensureDirectoryExists(outputDir);
            fileManager.ensureDoesNotExist(finalFile);
            fileManager.ensureDoesNotExist(tmpFile);

            csvWriter.writeReport(tmpFile, reportRows);
            fileManager.atomicMove(tmpFile, finalFile);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new ReportWriteException("Failed to write report", e);
        }

        return new ReportResult(finalFile, docs.size(), reportRows.size());
    }

    private List<DocumentRecord> fetchWithTimeout(final SearchCriteria criteria, int timeoutSeconds) {
        ExecutorService exec = Executors.newSingleThreadExecutor();
        Future<List<DocumentRecord>> f = exec.submit(new Callable<List<DocumentRecord>>() {
            @Override
            public List<DocumentRecord> call() {
                return connector.fetchDocuments(criteria);
            }
        });

        try {
            return f.get(timeoutSeconds, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            f.cancel(true);
            throw new FileNetQueryTimeoutException("FileNet query exceeded timeoutSeconds=" + timeoutSeconds, e);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
            throw new ReportWriteException("Unexpected exception during FileNet fetch", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ReportWriteException("Interrupted while waiting for FileNet fetch", e);
        } finally {
            exec.shutdownNow();
        }
    }

    public static class ReportResult {
        private final Path finalPath;
        private final int totalRowsFetched;
        private final int reportRowCount;

        public ReportResult(Path finalPath, int totalRowsFetched, int reportRowCount) {
            this.finalPath = finalPath;
            this.totalRowsFetched = totalRowsFetched;
            this.reportRowCount = reportRowCount;
        }

        public Path getFinalPath() {
            return finalPath;
        }

        public int getTotalRowsFetched() {
            return totalRowsFetched;
        }

        public int getReportRowCount() {
            return reportRowCount;
        }
    }
}


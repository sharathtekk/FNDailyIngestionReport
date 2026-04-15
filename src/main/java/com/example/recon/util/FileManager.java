package com.example.recon.util;

import com.example.recon.exception.ReportWriteException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.UUID;

@Component
public class FileManager {

    public Path resolveOutputDir(String configuredOutputPath) {
        if (configuredOutputPath == null || configuredOutputPath.trim().isEmpty()) {
            throw new ReportWriteException("Configured output path is blank");
        }
        return Paths.get(configuredOutputPath);
    }

    public void ensureDirectoryExists(Path dir) {
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            throw new ReportWriteException("Failed to create output directory dir=" + dir, e);
        }
    }

    public void ensureDoesNotExist(Path path) {
        if (Files.exists(path)) {
            throw new ReportWriteException("Refusing to overwrite existing file path=" + path);
        }
    }

    public void atomicMove(Path tempFile, Path finalFile) {
        try {
            // Best-effort atomic rename; requires same filesystem/directory.
            Files.move(tempFile, finalFile, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            // Assumption: On Windows/SMB, a same-directory rename is still effectively atomic,
            // even if ATOMIC_MOVE is not reported as supported.
            try {
                Files.move(tempFile, finalFile);
            } catch (IOException ioe) {
                throw new ReportWriteException("Failed to move tempFile=" + tempFile + " to finalFile=" + finalFile, ioe);
            }
        } catch (IOException e) {
            throw new ReportWriteException("Failed to move tempFile=" + tempFile + " to finalFile=" + finalFile, e);
        }
    }

    public String buildBaseFileName(String prefix, LocalDate fromDate, LocalDate toDate, UUID runId) {
        if (prefix == null || prefix.trim().isEmpty()) {
            throw new ReportWriteException("file prefix is blank");
        }
        if (fromDate == null || toDate == null) {
            throw new ReportWriteException("fromDate/toDate must not be null for file name");
        }
        if (runId == null) {
            throw new ReportWriteException("runId must not be null for file name");
        }
        // Idempotency: runId makes each run unique; no overwrites.
        return prefix + "_" + fromDate + "_" + toDate + "_" + runId;
    }
}


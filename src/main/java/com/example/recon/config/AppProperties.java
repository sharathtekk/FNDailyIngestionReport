package com.example.recon.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Validated
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    @Valid
    @NotNull
    private final Report report = new Report();

    @Valid
    @NotNull
    private final Query query = new Query();

    @Valid
    @NotNull
    private final Timeout timeout = new Timeout();

    @Valid
    @NotNull
    private final Execution execution = new Execution();

    @Valid
    @NotNull
    private final Filenet filenet = new Filenet();

    public Report getReport() {
        return report;
    }

    public Query getQuery() {
        return query;
    }

    public Timeout getTimeout() {
        return timeout;
    }

    public Execution getExecution() {
        return execution;
    }

    public Filenet getFilenet() {
        return filenet;
    }

    public static class Report {
        /**
         * Assumption: output path points to an SMB-mounted shared folder on Windows,
         * and the service account running this process has create/write/rename permissions.
         */
        @NotBlank
        private String outputPath;

        @NotBlank
        private String filePrefix;

        public String getOutputPath() {
            return outputPath;
        }

        public void setOutputPath(String outputPath) {
            this.outputPath = outputPath;
        }

        public String getFilePrefix() {
            return filePrefix;
        }

        public void setFilePrefix(String filePrefix) {
            this.filePrefix = filePrefix;
        }
    }

    public static class Query {
        @NotBlank
        private String dateField;

        @NotBlank
        private String formTypeField;

        @NotBlank
        private String sourceField;

        @NotBlank
        private String className;

        public String getDateField() {
            return dateField;
        }

        public void setDateField(String dateField) {
            this.dateField = dateField;
        }

        public String getFormTypeField() {
            return formTypeField;
        }

        public void setFormTypeField(String formTypeField) {
            this.formTypeField = formTypeField;
        }

        public String getSourceField() {
            return sourceField;
        }

        public void setSourceField(String sourceField) {
            this.sourceField = sourceField;
        }

        public String getClassName() {
            return className;
        }

        public void setClassName(String className) {
            this.className = className;
        }
    }

    public static class Timeout {
        @Min(1)
        private int filenetSeconds;

        public int getFilenetSeconds() {
            return filenetSeconds;
        }

        public void setFilenetSeconds(int filenetSeconds) {
            this.filenetSeconds = filenetSeconds;
        }
    }

    public static class Execution {
        @NotNull
        private Boolean retryEnabled;

        @NotNull
        private Boolean failFast;

        public Boolean getRetryEnabled() {
            return retryEnabled;
        }

        public void setRetryEnabled(Boolean retryEnabled) {
            this.retryEnabled = retryEnabled;
        }

        public Boolean getFailFast() {
            return failFast;
        }

        public void setFailFast(Boolean failFast) {
            this.failFast = failFast;
        }
    }

    public static class Filenet {
        /**
         * Assumption: if true, enable the real P8 connector bean and disable the stub.
         * This project uses reflection for the P8 Java API so it compiles without IBM jars.
         */
        @NotNull
        private Boolean enabled = Boolean.FALSE;

        /**
         * Assumption: The connector authenticates using JAAS Stanza + Subject.
         * Many FileNet deployments use UserContext + JAAS or WSI transport; details vary.
         * These values must match your environment.
         */
        @NotBlank
        private String ceUri = "http://localhost:9080/wsi/FNCEWS40MTOM/";

        @NotBlank
        private String objectStore = "OS1";

        @NotBlank
        private String jaasStanza = "FileNetP8WSI";

        @NotBlank
        private String username = "change-me";

        @NotBlank
        private String password = "change-me";

        public Boolean getEnabled() {
            return enabled;
        }

        public void setEnabled(Boolean enabled) {
            this.enabled = enabled;
        }

        public String getCeUri() {
            return ceUri;
        }

        public void setCeUri(String ceUri) {
            this.ceUri = ceUri;
        }

        public String getObjectStore() {
            return objectStore;
        }

        public void setObjectStore(String objectStore) {
            this.objectStore = objectStore;
        }

        public String getJaasStanza() {
            return jaasStanza;
        }

        public void setJaasStanza(String jaasStanza) {
            this.jaasStanza = jaasStanza;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}


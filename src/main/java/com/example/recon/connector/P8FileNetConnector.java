package com.example.recon.connector;

import com.example.recon.config.AppProperties;
import com.example.recon.dto.DocumentRecord;
import com.example.recon.dto.SearchCriteria;
import com.example.recon.exception.FileNetConnectionException;
import com.example.recon.exception.ReportWriteException;
import com.example.recon.util.FilenetSearchSqlDates;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * IBM FileNet P8 connector implementation using the official P8 Java API via reflection.
 *
 * Why reflection?
 * - The vendor jars are not distributed with this project.
 * - This keeps the build clean while still using real API class names/methods when present at runtime.
 *
 * Runtime requirement:
 * - Add FileNet P8 Java API jars to the classpath when app.filenet.enabled=true.
 *
 * Verified API entry points (IBM docs / examples):
 * - com.filenet.api.core.Factory.Connection.getConnection(String)
 * - com.filenet.api.util.UserContext.get()
 * - com.filenet.api.core.Factory.Domain.fetchInstance(Connection, String, PropertyFilter)
 * - com.filenet.api.core.Factory.ObjectStore.fetchInstance(Domain, String, PropertyFilter)
 * - com.filenet.api.query.SearchSQL (setQueryString)
 * - com.filenet.api.query.SearchScope.fetchRows(SearchSQL, Integer/0, PropertyFilter, Boolean)
 * - com.filenet.api.collection.RepositoryRowSet iteration + RepositoryRow.getProperties()
 * - com.filenet.api.property.Properties.getDateTimeValue / getStringValue
 */
@Component
@ConditionalOnProperty(prefix = "app.filenet", name = "enabled", havingValue = "true")
public class P8FileNetConnector implements FileNetConnector {

    private static final Logger log = LoggerFactory.getLogger(P8FileNetConnector.class);

    private final AppProperties props;

    public P8FileNetConnector(AppProperties props) {
        this.props = props;
    }

    @Override
    public List<DocumentRecord> fetchDocuments(SearchCriteria criteria) {
        if (criteria == null) {
            return new ArrayList<DocumentRecord>();
        }

        // NOTE: We intentionally keep this connector focused: connect + query + map fields.
        // Timeout is enforced by the caller (ReportService) via Future.get(timeout).
        try {
            Object connection = connect();
            Object objectStore = openObjectStore(connection);
            String query = buildQuery(criteria);
            return executeQuery(objectStore, query);
        } catch (FileNetConnectionException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new FileNetConnectionException("Unexpected FileNet connector error", e);
        }
    }

    private Object connect() {
        assertClassPresent("com.filenet.api.core.Factory");
        assertClassPresent("com.filenet.api.util.UserContext");

        try {
            // Connection conn = Factory.Connection.getConnection(ceUri);
            Class<?> factory = Class.forName("com.filenet.api.core.Factory");
            Class<?> factoryConnection = Class.forName("com.filenet.api.core.Factory$Connection");
            Method getConnection = factoryConnection.getMethod("getConnection", String.class);
            Object conn = getConnection.invoke(null, props.getFilenet().getCeUri());

            // Subject subject = UserContext.createSubject(conn, username, password, stanza);
            Class<?> userContext = Class.forName("com.filenet.api.util.UserContext");
            Method createSubject = userContext.getMethod("createSubject",
                    Class.forName("com.filenet.api.core.Connection"),
                    String.class, String.class, String.class);
            Object subject = createSubject.invoke(null,
                    conn,
                    props.getFilenet().getUsername(),
                    props.getFilenet().getPassword(),
                    props.getFilenet().getJaasStanza());

            // UserContext.get().pushSubject(subject);
            Method get = userContext.getMethod("get");
            Object uc = get.invoke(null);
            Method pushSubject = userContext.getMethod("pushSubject", javax.security.auth.Subject.class);
            pushSubject.invoke(uc, subject);

            return conn;
        } catch (InvocationTargetException e) {
            throw new FileNetConnectionException("FileNet connection/authentication failed", e.getTargetException());
        } catch (Exception e) {
            throw new FileNetConnectionException("FileNet connection/authentication failed", e);
        }
    }

    private Object openObjectStore(Object connection) {
        try {
            // Domain domain = Factory.Domain.fetchInstance(conn, null, null);
            Class<?> factoryDomain = Class.forName("com.filenet.api.core.Factory$Domain");
            Method fetchDomain = factoryDomain.getMethod("fetchInstance",
                    Class.forName("com.filenet.api.core.Connection"),
                    String.class,
                    Class.forName("com.filenet.api.property.PropertyFilter"));
            Object domain = fetchDomain.invoke(null, connection, null, null);

            // ObjectStore os = Factory.ObjectStore.fetchInstance(domain, objectStoreName, null);
            Class<?> factoryOs = Class.forName("com.filenet.api.core.Factory$ObjectStore");
            Method fetchOs = factoryOs.getMethod("fetchInstance",
                    Class.forName("com.filenet.api.core.Domain"),
                    String.class,
                    Class.forName("com.filenet.api.property.PropertyFilter"));
            return fetchOs.invoke(null, domain, props.getFilenet().getObjectStore(), null);
        } catch (InvocationTargetException e) {
            throw new FileNetConnectionException("Failed opening object store", e.getTargetException());
        } catch (Exception e) {
            throw new FileNetConnectionException("Failed opening object store", e);
        }
    }

    private String buildQuery(SearchCriteria c) {
        // Content Engine SearchSQL: do not use DATE('yyyy-MM-dd') — not valid; use UTC literals.
        // No GROUP BY/COUNT in CE SQL for arbitrary document queries; we SELECT rows and aggregate in Java.
        // fromDate inclusive (midnight UTC), toDate exclusive (midnight UTC); see FilenetSearchSqlDates.
        String dateField = "[" + props.getQuery().getDateField() + "]";
        String formTypeField = "[" + props.getQuery().getFormTypeField() + "]";
        String sourceField = "[" + props.getQuery().getSourceField() + "]";
        String className = "[" + props.getQuery().getClassName() + "]";

        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ");
        sb.append(dateField).append(", ");
        sb.append(formTypeField).append(", ");
        sb.append(sourceField);
        sb.append(" FROM ").append(className);
        sb.append(" WHERE ");
        sb.append(dateField).append(" >= ").append(FilenetSearchSqlDates.utcStartOfDayLiteral(c.getFromDate()));
        sb.append(" AND ");
        sb.append(dateField).append(" < ").append(FilenetSearchSqlDates.utcStartOfDayLiteral(c.getToDate()));

        if (c.getFormType() != null) {
            sb.append(" AND ").append(formTypeField).append(" = ").append(toStringLiteral(c.getFormType()));
        }
        if (c.getSource() != null) {
            sb.append(" AND ").append(sourceField).append(" = ").append(toStringLiteral(c.getSource()));
        }

        sb.append(" OPTIONS(TIMELIMIT 180)");

        String query = sb.toString();
        log.info("event=filenet_query_built ceUri={} objectStore={} query=\"{}\"",
                props.getFilenet().getCeUri(), props.getFilenet().getObjectStore(), query);
        return query;
    }

    private List<DocumentRecord> executeQuery(Object objectStore, String queryString) throws Exception {
        assertClassPresent("com.filenet.api.query.SearchSQL");
        assertClassPresent("com.filenet.api.query.SearchScope");
        assertClassPresent("com.filenet.api.collection.RepositoryRowSet");

        // SearchSQL sql = new SearchSQL(queryString);
        Class<?> searchSqlClass = Class.forName("com.filenet.api.query.SearchSQL");
        Object searchSql;
        try {
            searchSql = searchSqlClass.getConstructor(String.class).newInstance(queryString);
        } catch (NoSuchMethodException e) {
            // Some examples use new SearchSQL(); sql.setQueryString(...)
            searchSql = searchSqlClass.newInstance();
            Method setQueryString = searchSqlClass.getMethod("setQueryString", String.class);
            setQueryString.invoke(searchSql, queryString);
        }

        // SearchScope scope = new SearchScope(objectStore);
        Class<?> searchScopeClass = Class.forName("com.filenet.api.query.SearchScope");
        Object searchScope = searchScopeClass.getConstructor(Class.forName("com.filenet.api.core.ObjectStore"))
                .newInstance(objectStore);

        // RepositoryRowSet rowSet = scope.fetchRows(searchSql, 0, null, false);
        Method fetchRows = findFetchRows(searchScopeClass);
        Object rowSet = fetchRows.invoke(searchScope, searchSql, Integer.valueOf(0), null, Boolean.FALSE);

        // Iterate results: RepositoryRow -> Properties -> map fields.
        Method iteratorM = rowSet.getClass().getMethod("iterator");
        Iterator<?> it = (Iterator<?>) iteratorM.invoke(rowSet);

        List<DocumentRecord> out = new ArrayList<DocumentRecord>();
        while (it.hasNext()) {
            Object row = it.next(); // com.filenet.api.query.RepositoryRow
            Method getProperties = row.getClass().getMethod("getProperties");
            Object propsObj = getProperties.invoke(row); // com.filenet.api.property.Properties

            LocalDate dateCreated = readLocalDate(propsObj, props.getQuery().getDateField());
            String formType = readString(propsObj, props.getQuery().getFormTypeField());
            String source = readString(propsObj, props.getQuery().getSourceField());

            out.add(new DocumentRecord(dateCreated, formType, source));
        }

        return out;
    }

    private Method findFetchRows(Class<?> searchScopeClass) throws NoSuchMethodException, ClassNotFoundException {
        // IBM examples show fetchRows(SearchSQL, int/Integer, PropertyFilter, boolean/Boolean)
        try {
            return searchScopeClass.getMethod("fetchRows",
                    Class.forName("com.filenet.api.query.SearchSQL"),
                    int.class,
                    Class.forName("com.filenet.api.property.PropertyFilter"),
                    boolean.class);
        } catch (NoSuchMethodException ignored) {
            // Try boxed signatures
        }
        return searchScopeClass.getMethod("fetchRows",
                Class.forName("com.filenet.api.query.SearchSQL"),
                Integer.class,
                Class.forName("com.filenet.api.property.PropertyFilter"),
                Boolean.class);
    }

    private LocalDate readLocalDate(Object filenetProperties, String propertyName) {
        if (filenetProperties == null) {
            return null;
        }
        try {
            // DateTime getDateTimeValue(String)
            Method getDateTimeValue = filenetProperties.getClass().getMethod("getDateTimeValue", String.class);
            Object dt = getDateTimeValue.invoke(filenetProperties, propertyName);
            if (dt == null) {
                return null;
            }
            java.util.Date d;
            if (dt instanceof java.util.Date) {
                // Some versions return java.util.Date directly
                d = (java.util.Date) dt;
            } else {
                // com.filenet.api.util.DateTime#toJavaUtilDate()
                Method toJavaUtilDate = dt.getClass().getMethod("toJavaUtilDate");
                d = (java.util.Date) toJavaUtilDate.invoke(dt);
            }
            if (d == null) {
                return null;
            }
            // Align report "DateCreated" bucket with UTC date (same basis as query literals).
            return Instant.ofEpochMilli(d.getTime()).atZone(ZoneOffset.UTC).toLocalDate();
        } catch (Exception e) {
            throw new ReportWriteException("Failed reading date propertyName=" + propertyName, e);
        }
    }

    private String readString(Object filenetProperties, String propertyName) {
        if (filenetProperties == null) {
            return null;
        }
        try {
            Method getStringValue = filenetProperties.getClass().getMethod("getStringValue", String.class);
            Object v = getStringValue.invoke(filenetProperties, propertyName);
            return v == null ? null : String.valueOf(v);
        } catch (Exception e) {
            throw new ReportWriteException("Failed reading string propertyName=" + propertyName, e);
        }
    }

    private String toStringLiteral(String s) {
        if (s == null) {
            return "''";
        }
        return "'" + s.replace("'", "''") + "'";
    }

    private void assertClassPresent(String fqcn) {
        try {
            Class.forName(fqcn);
        } catch (ClassNotFoundException e) {
            throw new FileNetConnectionException("Missing FileNet API class on classpath: " + fqcn, e);
        }
    }
}


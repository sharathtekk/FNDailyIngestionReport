# FileNet Reconciliation Report (Spring Boot 2.7 / Java 8)

## Externalizing configuration (no rebuild needed)

This app supports running with an **external** `application.yml`, so you can change configuration values without rebuilding the JAR.

- **External config path**: `.\config\application.yml` (relative to where you run the command)
- **How it’s loaded**: we pass `--spring.config.additional-location=file:./config/` at startup

### Run on Windows

From the project root:

```bat
mvn test
mvn install

run.cmd --fromDate=2026-01-01 --toDate=2026-01-02
```

Or with PowerShell:

```powershell
mvn test
mvn install

.\run.ps1 --fromDate=2026-01-01 --toDate=2026-01-02
```

### Precedence

Spring Boot will still read the `application.yml` packaged inside the JAR, but the **external** config in `.\config\` will override it when keys overlap.

## FileNet CE client JARs (external `lib\` folder)

IBM CE client JARs (**`Jace.jar`** and any other jars your site requires from the same bundle) stay **outside** the application JAR, under `lib\`. See `lib\README.txt`.

The executable JAR is built with Spring Boot **layout ZIP** (`PropertiesLauncher`). `run.cmd` / `run.ps1` start the process with **`-Dloader.path=lib`**, so everything in `lib\` is on the classpath at runtime.

```bat
mvn clean install
run.cmd --fromDate=2026-01-01 --toDate=2026-01-02
```

If you run **without** IBM JARs on the classpath, set `app.filenet.enabled=false` in `config\application.yml` so the **stub** connector is used.

**Log4j / `NoClassDefFoundError: …Priority`:** IBM Jace expects Log4j 1.x API classes (`org.apache.log4j.*`). This project includes **reload4j** in `pom.xml` (packaged in the app JAR). Rebuild after pulling changes: `mvn clean install`.

**FileNet SearchSQL dates:** Content Engine SQL is not generic SQL — do not use `DATE('yyyy-mm-dd')`. The connector uses UTC midnight literals (e.g. `20260102T000000Z`). Pass **`--toDate` as the exclusive end** (for one calendar day: `--fromDate=2026-01-02 --toDate=2026-01-03`). **GROUP BY / COUNT** are not used in CE SQL here; rows are fetched and aggregated in Java (`ReconciliationService`).


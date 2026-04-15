@echo off
setlocal

REM Runs the app with:
REM   - External config: .\config\application.yml
REM   - External FileNet / IBM CE client JARs: .\lib\ (Jace.jar and any other required jars from IBM)
REM Uses Spring Boot PropertiesLauncher (-Dloader.path=lib) so lib/ is NOT inside the application JAR.

set JAR=target\filenet-recon-report-0.0.1-SNAPSHOT.jar

if not exist "%JAR%" (
  echo [ERROR] JAR not found at %JAR%
  echo Build it first: mvn -DskipTests=false clean install
  exit /b 1
)

java -Dloader.path=lib -jar "%JAR%" --spring.config.additional-location=file:./config/ %*

@REM ----------------------------------------------------------------------------
@REM Maven Wrapper startup batch script, version 3.2.0
@REM ----------------------------------------------------------------------------
@echo off
setlocal

set MVNW_REPOURL=
set MVNW_VERBOSE=false

set WRAPPER_JAR=.mvn\wrapper\maven-wrapper.jar
set WRAPPER_PROPERTIES=.mvn\wrapper\maven-wrapper.properties

if not exist "%WRAPPER_PROPERTIES%" (
  echo [ERROR] Missing %WRAPPER_PROPERTIES%
  exit /b 1
)

for /f "usebackq tokens=1,2 delims==" %%A in ("%WRAPPER_PROPERTIES%") do (
  if "%%A"=="wrapperUrl" set WRAPPER_URL=%%B
)

if "%WRAPPER_URL%"=="" (
  echo [ERROR] wrapperUrl not set in %WRAPPER_PROPERTIES%
  exit /b 1
)

if not exist "%WRAPPER_JAR%" (
  if "%MVNW_VERBOSE%"=="true" echo Downloading Maven Wrapper from %WRAPPER_URL%
  powershell -NoProfile -ExecutionPolicy Bypass -Command ^
    "$ProgressPreference='SilentlyContinue';" ^
    "$url='%WRAPPER_URL%';" ^
    "$out='%WRAPPER_JAR%';" ^
    "New-Item -ItemType Directory -Force -Path (Split-Path $out) | Out-Null;" ^
    "Invoke-WebRequest -Uri $url -OutFile $out"
  if errorlevel 1 (
    echo [ERROR] Failed downloading %WRAPPER_URL%
    exit /b 1
  )
)

set MAVEN_PROJECTBASEDIR=%~dp0
if "%MAVEN_PROJECTBASEDIR:~-1%"=="\" set MAVEN_PROJECTBASEDIR=%MAVEN_PROJECTBASEDIR:~0,-1%

set JVM_CONFIG_MAVEN_OPTS=
if exist "%MAVEN_PROJECTBASEDIR%\.mvn\jvm.config" (
  set /p JVM_CONFIG_MAVEN_OPTS=<"%MAVEN_PROJECTBASEDIR%\.mvn\jvm.config"
)

set MAVEN_OPTS=%JVM_CONFIG_MAVEN_OPTS% %MAVEN_OPTS%

java -jar "%WRAPPER_JAR%" -Dmaven.multiModuleProjectDirectory="%MAVEN_PROJECTBASEDIR%" %*
exit /b %errorlevel%


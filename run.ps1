param(
  [Parameter(ValueFromRemainingArguments = $true)]
  [string[]]$Args
)

# Runs with external config and external lib/ (IBM CE client JARs) via PropertiesLauncher.

$jar = "target\filenet-recon-report-0.0.1-SNAPSHOT.jar"
if (-not (Test-Path $jar)) {
  Write-Error "JAR not found at $jar. Build it first: mvn clean install"
  exit 1
}

& java "-Dloader.path=lib" -jar $jar "--spring.config.additional-location=file:./config/" @Args
exit $LASTEXITCODE

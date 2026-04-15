#!/bin/sh
# ----------------------------------------------------------------------------
# Maven Wrapper startup script, version 3.2.0
# ----------------------------------------------------------------------------

MVNW_DIR="$(cd "$(dirname "$0")" && pwd)"
WRAPPER_JAR="$MVNW_DIR/.mvn/wrapper/maven-wrapper.jar"
WRAPPER_PROPERTIES="$MVNW_DIR/.mvn/wrapper/maven-wrapper.properties"

if [ ! -f "$WRAPPER_PROPERTIES" ]; then
  echo "[ERROR] Missing $WRAPPER_PROPERTIES" >&2
  exit 1
fi

WRAPPER_URL=$(grep '^wrapperUrl=' "$WRAPPER_PROPERTIES" | cut -d= -f2-)
if [ -z "$WRAPPER_URL" ]; then
  echo "[ERROR] wrapperUrl not set in $WRAPPER_PROPERTIES" >&2
  exit 1
fi

if [ ! -f "$WRAPPER_JAR" ]; then
  mkdir -p "$(dirname "$WRAPPER_JAR")"
  if command -v curl >/dev/null 2>&1; then
    curl -fsSL "$WRAPPER_URL" -o "$WRAPPER_JAR"
  elif command -v wget >/dev/null 2>&1; then
    wget -q "$WRAPPER_URL" -O "$WRAPPER_JAR"
  else
    echo "[ERROR] Need curl or wget to download Maven Wrapper" >&2
    exit 1
  fi
fi

exec java -jar "$WRAPPER_JAR" -Dmaven.multiModuleProjectDirectory="$MVNW_DIR" "$@"


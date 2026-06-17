#!/usr/bin/env bash
#
# Installs the collector's dependencies into the project-local Maven repository (./repo)
# so the build can resolve them without any remote repositories.
#
# The open-source jars (slf4j-api, jettison) are committed to ./repo and normally need not
# be reinstalled. The proprietary OpenText/NetIQ jars are git-ignored and MUST be installed
# locally by each developer from their IG / IDM installation.
#
# Usage:
#   ./install-local-deps.sh [SOURCE_DIR]
#
# SOURCE_DIR defaults to ~/Dev/igCollectorDependencies and must contain the jar files listed
# below. See README.md ("Build instructions") for where to obtain them.
set -euo pipefail

SRC="${1:-$HOME/Dev/igCollectorDependencies}"
REPO="$(cd "$(dirname "$0")" && pwd)/repo"

if [[ ! -d "$SRC" ]]; then
  echo "ERROR: dependency source directory not found: $SRC" >&2
  echo "Pass the directory containing the jars as the first argument." >&2
  exit 1
fi

# coordinate|file mappings: groupId:artifactId:version|jarFileName
DEPS=(
  "org.slf4j:slf4j-api:1.7.22|slf4j-api-1.7.22.jar"
  "org.codehaus.jettison:jettison:1.3.7|jettison-1.3.7.jar"
  "com.opentext.ig:daas-sdkserver:3.6.1|DaaS-SDKServer.jar"
  "com.netiq.dirxml:xds:4.8.0.0|XDS-4.8.0.0.jar"
  "com.netiq.dirxml:dirxml-misc:4.8.3.0|dirxml_misc.jar"
  "com.netiq.ism:logging-common:1.4.2-57|logging-common-1.4.2-57.jar"
  "com.novell:ldap:1.0|ldap.jar"
)

for entry in "${DEPS[@]}"; do
  coord="${entry%%|*}"
  jar="${entry##*|}"
  IFS=':' read -r groupId artifactId version <<< "$coord"
  jarPath="$SRC/$jar"
  if [[ ! -f "$jarPath" ]]; then
    echo "ERROR: missing $jar in $SRC (for $coord)" >&2
    exit 1
  fi
  echo ">> installing $coord  <-  $jar"
  mvn -q org.apache.maven.plugins:maven-install-plugin:install-file \
    -Dfile="$jarPath" \
    -DgroupId="$groupId" \
    -DartifactId="$artifactId" \
    -Dversion="$version" \
    -Dpackaging=jar \
    -DgeneratePom=true \
    -DcreateChecksum=true \
    -DlocalRepositoryPath="$REPO"
done

echo "Done. Dependencies installed into $REPO"

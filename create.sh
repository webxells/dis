#!/usr/bin/env bash

log() { echo "[$(date '+%H:%M:%S')] $*"; }
die() { echo "ERROR: $*" >&2; exit 1; }

DIR="$(dirname "$0")"
TMP=$(mktemp -d)
SESSION="$1"
if [ -z "$SESSION" ]; then
  die "Usage: $0 {forgejo session-id}"
fi

log "Working directory: $(pwd)"

REPOS=("api" "pom" "pom-definitions" "handler-officex" "info" "trigger-rest" "test-example" "handler-okhttp" "handler-rest" "base" "boot" "workflow-service" "event" "handler-plain" "handler-csv" "handler-gis" "handler-google" "logging-simple" "handler-time" "mail" "handler-localfile" "resource-sftp" "handler-json" "handler-sql" "handler-fileregistry" "logging" "logging-freeloader-slf4j" "logging-freeloader-log4j2" "logging-freeloader-apachecommons" "config-json" "langchain4j" "handler-memory" "handler-xml" "hash")

for repo in "${REPOS[@]}"; do
  log "Creating $repo"
  if [ -d "./$repo" ]; then
    log "Deleting old repo"
    rm -rf "./$repo" || die "Could not delete repo"
  fi
  wget -q --header="Cookie: session=$SESSION" "https://code.webxells.com/dis/$repo/archive/master.zip" -O "$TMP/temp.zip" || die "Could not load files"
  unzip "$TMP/temp.zip" -x "$repo/.forgejo/*" "$repo/.gitignore" || die "Could not unzip files"
  log "Adding copyright to java files..."
  while IFS= read -r java; do
    if head -n1 "$java" | grep -Fq "/*"; then
     log "Copyright already present: $java"
    else
     cat "$DIR/LICENSE_FILE" <(echo) "$java" > "$TMP/temp"
     mv "$TMP/temp" "$java"
    fi
  done < <(find "$repo/src" -name '*.java')
done

rm -rf "$TMP"
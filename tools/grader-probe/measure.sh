#!/usr/bin/env bash
# Measures the probe once and appends one line to results.tsv. Needs no secret.
#
# Usage:
#   PROBE_URL=https://<grader-probe>.onrender.com \
#   BACKEND_URL=https://<backend-standin>.onrender.com \
#   ./measure.sh <label>
#
# label: "cold" when nothing called the services for at least 20 minutes, otherwise "warm".
# BACKEND_URL is optional. With it, the backend is woken first and the grader only after the backend
# answered: the worst case, a Submit that is the first request after a pause.
#
# Columns (tab-separated): start time (UTC), label, backend seconds/HTTP code (or -),
# grader /health seconds/HTTP code, slowest /health while the self-test ran (seconds), the /stats JSON.
set -euo pipefail
: "${PROBE_URL:?set PROBE_URL}"
label="${1:?give a label: cold or warm}"
started="$(date -u +%Y-%m-%dT%H:%M:%SZ)"

backend="-"
if [ -n "${BACKEND_URL:-}" ]; then
  backend="$(curl -sS --max-time 400 -o /dev/null -w '%{time_total}/%{http_code}' "$BACKEND_URL/actuator/health/liveness" || true)"
fi
health="$(curl -sS --max-time 300 -o /dev/null -w '%{time_total}/%{http_code}' "$PROBE_URL/health" || true)"

# The self-test runs after every start. Until it is done (at most about 5 minutes), keep timing
# /health: it must keep answering while a child starts, or Render's health check could fail.
slowest=0
stats=""
for _ in $(seq 1 60); do
  t="$(curl -sS --max-time 30 -o /dev/null -w '%{time_total}' "$PROBE_URL/health" || echo 30)"
  slowest="$(awk -v a="$slowest" -v b="$t" 'BEGIN { print (b > a) ? b : a }')"
  stats="$(curl -sS --max-time 30 "$PROBE_URL/stats" | tr -d '\n' || true)"
  case "$stats" in
    *'"state":"running"'*) sleep 5 ;;
    *) break ;;
  esac
done

printf '%s\t%s\t%s\t%s\t%s\t%s\n' "$started" "$label" "$backend" "$health" "$slowest" "$stats" | tee -a results.tsv

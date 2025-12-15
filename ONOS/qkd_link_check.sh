#!/usr/bin/env bash
# --- auto-load .env (project root, stessa cartella, cwd) ---
set -Eeuo pipefail
SDIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
for ENV_CAND in "$SDIR/.env" "$SDIR/../.env" "$PWD/.env"; do
  if [[ -f "$ENV_CAND" ]]; then
    set -a; . "$ENV_CAND"; set +a
    break
  fi
done

# default di sicurezza se qualche variabile manca
AUTH_USER="${AUTH_USER:-karaf}"
AUTH_PASS="${AUTH_PASS:-karaf}"
BASE="${BASE:-http://localhost:8181/onos/quantum-app}"
ONOS="${ONOS:-http://localhost:8181/onos}"
ID="${ID:-bbbbbbbb-0031-bbbb-0032-bbbbbbbbbbbb}"

# Device (modifica se servono altri)
# Lista di device: se DEVICES è valorizzata nello .env (spazio-separata), usala; altrimenti default a 2 device
if [[ -n "${DEVICES:-}" ]]; then
  read -r -a DEVICES <<< "$DEVICES"
else
  DEVICES=("netconf:172.25.0.101:830" "netconf:172.25.0.102:830")
fi


LOG="$SDIR/qkd_run_$(date +%Y%m%d_%H%M%S).log"


# ========= Utility =========
ts() { date '+%Y-%m-%d %H:%M:%S'; }
say() { echo "[$(ts)] $*" | tee -a "$LOG"; }
auth_args=(-u "$AUTH_USER:$AUTH_PASS")

pretty() {
  if command -v python3 >/dev/null 2>&1; then
    python3 -m json.tool || cat
  elif command -v python >/dev/null 2>&1; then
    python -m json.tool || cat
  else
    cat
  fi
}

curl_iS() { curl -iS "${auth_args[@]}" "$@"; }
curl_s()  { curl -s  "${auth_args[@]}" "$@"; }

# ========= Funzioni =========
check_wadl() {
  say "Scarico WADL e cerco le risorse note…"
  curl_s -H 'Accept: application/vnd.sun.wadl+xml' "$BASE/application.wadl" \
    | sed 's/></>\n</g' \
    | grep -i -E 'resource path=|method id=' \
    | tee -a "$LOG" || true
}

check_options() {
  for ep in activateLink deactivateLink getLinks; do
    say "OPTIONS links/$ep"
    curl_iS -X OPTIONS "$BASE/links/$ep" | tee -a "$LOG"
  done
}

show_links() {
  say "GET links/getLinks"
  curl_s "$BASE/links/getLinks" | pretty | tee -a "$LOG" || true
}

activate_link() {
  say "POST links/activateLink?key=$ID"
  curl_iS -H 'Accept: application/json' -X POST \
    "$BASE/links/activateLink?key=$ID" | tee -a "$LOG"
}

deactivate_link() {
  say "DELETE links/deactivateLink?key=$ID"
  curl_iS -H 'Accept: application/json' -X DELETE \
    "$BASE/links/deactivateLink?key=$ID" | tee -a "$LOG"
}

show_intents() {
  say "GET v1/intents?appId=org.quantum.app"
  curl_s "$ONOS/v1/intents?appId=org.quantum.app" | pretty | tee -a "$LOG" || true
}

show_flows() {
  for d in "${DEVICES[@]}"; do
    say "GET v1/flows/$d"
    curl_s "$ONOS/v1/flows/$d" | pretty | tee -a "$LOG" || true
  done
}

usage() {
  cat <<EOF
Uso:
  $(basename "$0") [check|activate|deactivate|status|full]

  check      -> WADL + OPTIONS sugli endpoint
  activate   -> attiva il link \$ID
  deactivate -> disattiva il link \$ID
  status     -> mostra links, intents e flows
  full       -> check + activate + status

Variabili (override via ENV):
  AUTH_USER/AUTH_PASS (default: karaf/karaf)
  BASE (default: $BASE)
  ONOS (default: $ONOS)
  ID (default: $ID)
  DEVICES (default: "${DEVICES[*]}")

Log: $LOG
EOF
}

cmd="${1:-full}"
case "$cmd" in
  check)
    check_wadl
    check_options
    ;;
  activate)
    activate_link
    ;;
  deactivate)
    deactivate_link
    ;;
  status)
    show_links
    show_intents
    show_flows
    ;;
  full)
    check_wadl
    check_options
    activate_link
    show_links
    show_intents
    show_flows
    ;;
  *)
    usage
    exit 1
    ;;
esac

say "Fatto."

#!/usr/bin/env bash
set -euo pipefail


AUTH="-u karaf:karaf"
BASE_URL="http://localhost:8181/onos/quantum-app"


LINK_ID="bbbbbbbb-0031-bbbb-0032-bbbbbbbbbbbb"

echo "[1/3] Controllo che la Webapp 'quantum-app' sia pronta..."

curl -sf $AUTH -H 'Accept: application/vnd.sun.wadl+xml' "$BASE_URL/application.wadl" > /dev/null
echo "==> App 'quantum-app' pronta."
echo ""

echo "[2/3] Controllo che il link $LINK_ID esista (creato da init-network.sh)..."

if curl -sf $AUTH "$BASE_URL/links/getLinks" | grep -q "$LINK_ID"; then
  echo "==> Link $LINK_ID trovato."
else
  echo "ERRORE: Link $LINK_ID non trovato."
  echo "Assicurati di aver eseguito ./init-network.sh prima di questo script."
  exit 1
fi
echo ""

echo "[3/3] Invio comando di ATTIVAZIONE per il link $LINK_ID..."

RESPONSE=$(curl -sf $AUTH -H 'Accept: application/json' -X POST \
  "$BASE_URL/links/activateLink?key=$LINK_ID")

echo "==> Risposta dal server:"
echo "$RESPONSE"
echo ""


if echo "$RESPONSE" | grep -q "$LINK_ID"; then
  echo "==> SUCCESSO: Attivazione confermata."
  echo "Controlla i log dei container ETSI per vedere lo scambio chiavi."
else
  echo "ERRORE: La risposta del server non ha confermato l'attivazione."
fi

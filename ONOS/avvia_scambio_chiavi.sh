#!/usr/bin/env bash
set -euo pipefail
# Script per creare e attivare i link QKD dopo init-network.sh

AUTH="-u karaf:karaf"
BASE_URL="http://localhost:8181/onos/quantum-app"

# Coppia 1: etsi1 <-> etsi2
SRC1="netconf:172.25.0.101:830/1"
DST1="netconf:172.25.0.102:830/2"

# Coppia 2: etsi1 <-> etsi3
SRC2="netconf:172.25.0.101:830/2"
DST2="netconf:172.25.0.103:830/1"

# Funzione per creare e attivare un link
activate_link() {
    local src_cp=$1
    local dst_cp=$2
    echo "--- Attivazione link $src_cp -> $dst_cp ---"

    # 1. Crea il link
    JSON_BODY=$(printf '{"srcConnectPoint": "%s", "dstConnectPoint": "%s"}' "$src_cp" "$dst_cp")
    CREATE_RESPONSE=$(curl -sf $AUTH -H 'Content-Type: application/json' -X POST -d "$JSON_BODY" "$BASE_URL/links/createDirectLink")

    LINK_ID=$(echo "$CREATE_RESPONSE" | python3 -c "import sys, json; print(json.load(sys.stdin).get('link_id', ''))")

    if [ -z "$LINK_ID" ]; then
        echo "ERRORE: Impossibile creare il link. Risposta: $CREATE_RESPONSE"
        return 1
    fi
    echo "Link creato con ID: $LINK_ID"
    sleep 1

    # 2. Attiva il link
    ACTIVATE_RESPONSE=$(curl -sf $AUTH -H 'Accept: application/json' -X POST "$BASE_URL/links/activateLink?key=$LINK_ID")

    if echo "$ACTIVATE_RESPONSE" | grep -q "$LINK_ID"; then
        echo "SUCCESSO: Attivazione confermata per $LINK_ID."
    else
        echo "ERRORE: Attivazione fallita. Risposta: $ACTIVATE_RESPONSE"
    fi
}

# Attiva tutti i link necessari
activate_link $SRC1 $DST1
activate_link $SRC2 $DST2

echo "--- Script di attivazione completato. ---"

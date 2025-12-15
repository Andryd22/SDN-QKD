#!/bin/bash

# Controllo sui parametri
if [ "$#" -ne 6 ]; then
    echo "Uso: $0 <qkdl_id> <qkdl_enable> <qkdl_status> <qkdl_auth_status> <IP> <PORT>"
    exit 1
fi

QKDL_ID="$1"
QKDL_ENABLE="$2"
QKDL_STATUS="$3"
QKDL_AUTH_STATUS="$4"
IP="$5"
PORT="$6"

# Creazione file temporaneo XML
TMPFILE=$(mktemp)

cat > "$TMPFILE" <<EOF
<?xml version="1.0"?>
<edit-config xmlns="urn:ietf:params:xml:ns:netconf:base:1.0">
  <target>
    <running/>
  </target>
  <config>
    <qkd_node xmlns="urn:etsi:qkd:yang:etsi-qkd-node">
      <qkd_links>
        <qkd_link>
          <qkdl_id>${QKDL_ID}</qkdl_id>
          <qkdl_status xmlns:etsi-qkdn-types="urn:etsi:qkd:yang:etsi-qkd-node-types">etsi-qkdn-types:${QKDL_STATUS}</qkdl_status>
          <qkdl_enable>${QKDL_ENABLE}</qkdl_enable>
          <qkdl_auth_status xmlns:etsi-qkdn-types="urn:etsi:qkd:yang:etsi-qkd-node-types">etsi-qkdn-types:${QKDL_AUTH_STATUS}</qkdl_auth_status>
        </qkd_link>
      </qkd_links>
    </qkd_node>
  </config>
</edit-config>
EOF

# Invio con netconf-console2
netconf-console2 --host "$IP" --port "$PORT" -u root -p root --rpc "$TMPFILE"


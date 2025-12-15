#! /bin/bash

echo "Posting openroadm ROADMs..."
onos-netcfg localhost ./b5g_roadm_devices.json
sleep 2

echo "Configuring etsi qkd EMULATED node identifiers..."
./configure-qkd-nodes.sh
sleep 2

echo "Configuring etsi qkd REAL node identifiers..."
./configure-qkd-nodes-sarosh.sh
sleep 2

echo "Posting etsi EMULATED qkd nodes..."
onos-netcfg localhost ./quancom_qkd_devices.json
sleep 2

echo "Posting etsi REAL qkd nodes..."
onos-netcfg localhost ./quancom_qkd_devices_sarosh.json
sleep 2

echo "Posting emulated fiber links..."
onos-netcfg localhost ./icton2025_links.json
sleep 2

echo "Posting emulated fiber links..."
onos-netcfg localhost ./icton2025_links_sarosh.json
sleep 2

echo "ETSI 015 --- QKD REST APIs..."
echo "--- deactivate"
onos-app localhost deactivate org.quantum.app
sleep 2

echo "--- uninstall"
onos-app localhost uninstall org.quantum.app
sleep 2

echo "--- install!"
onos-app localhost install! /home/alessio/onos-quancom/quantum-app/target/quantum-app-1.0-SNAPSHOT.oar
sleep 2

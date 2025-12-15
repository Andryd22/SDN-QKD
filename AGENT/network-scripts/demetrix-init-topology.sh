#! /bin/bash

echo "Configuring etsi qkd PHYSICAL node identifiers..."
./configure-qkd-nodes-demetrix.sh
sleep 2

echo "Posting etsi PoliMi qkd nodes..."
onos-netcfg localhost ./quancom_demetrix_devices.json
sleep 10

echo "Posting polimi fiber links..." 
onos-netcfg localhost ./quancom_demetrix_links.json
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
sleep 10

echo "Posting Key Managers on emulated qkd nodes..."
./demo-ofc2024-nodes-demetrix.curl.bash
sleep 2


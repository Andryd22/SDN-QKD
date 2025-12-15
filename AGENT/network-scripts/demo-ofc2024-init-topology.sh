#! /bin/bash

echo "Posting openroadm ROADMs..."
onos-netcfg localhost ./b5g_roadm_devices.json
sleep 2

echo "Posting openconfig transponders..."
onos-netcfg localhost ./b5g_transponder_devices.json
sleep 2

echo "Configuring etsi qkd EMULATED node identifiers..."
./configure-qkd-nodes.sh
sleep 2

echo "Configuring etsi qkd PHYSICAL node identifiers..."
./configure-qkd-nodes-polimi.sh
sleep 2

echo "Posting etsi emulated qkd nodes..."
onos-netcfg localhost ./quancom_qkd_devices.json
sleep 2

echo "Posting etsi PoliMi qkd nodes..."
onos-netcfg localhost ./quancom_qkd_polimi_devices.json
sleep 10

echo "Posting emulated fiber links..." 
onos-netcfg localhost ./b5g_links.json
sleep 2

echo "Posting polimi fiber links..." 
onos-netcfg localhost ./quancom_polimi_links.json
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
./demo-ofc2024-nodes-emulated.curl.bash
sleep 2

echo "Posting Key Managers on PoliMi qkd nodes..."
./demo-ofc2024-nodes-polimi.curl.bash
sleep 4

#echo "Creating QKD links..."
#./demo-ofc2024-links-create.curl.bash
#sleep 4

#echo "Activating QKD links..."
#./demo-ofc2024-links-activate.curl.bash
#sleep 2

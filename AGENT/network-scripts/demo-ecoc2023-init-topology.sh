#! /bin/bash

echo "Posting openroadm ROADMs..."
onos-netcfg localhost ./b5g_roadm_devices.json
sleep 2

echo "Posting openconfig transponders..."
onos-netcfg localhost ./b5g_transponder_devices.json
sleep 2

echo "Configuring etsi qkd node identifiers emulated..."
./configureTopoQUANCOM.sh
sleep 2

echo "Configuring etsi qkd node identifiers polimi..."
./configureTopoQUANCOM_polimi.sh 
sleep 2

echo "Posting etsi emulated qkd nodes..."
onos-netcfg localhost ./quancom_qkd_devices.json
sleep 2

echo "Posting etsi PoliMi qkd nodes..."
onos-netcfg localhost ./quancom_qkd_polimi_devices.json
sleep 5

echo "Posting emulated fiber links..." 
onos-netcfg localhost ./b5g_links.json
sleep 2

echo "Posting polimi fiber links..." 
onos-netcfg localhost ./quancom_polimi_links.json
sleep 2

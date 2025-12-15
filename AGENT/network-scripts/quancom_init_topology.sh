#! /bin/bash

echo "Posting openroadm ROADMs..."
onos-netcfg localhost ./b5g_roadm_devices.json
sleep 2

echo "Posting openconfig transponders..."
onos-netcfg localhost ./b5g_transponder_devices.json
sleep 2

echo "Posting etsi emulated qkd nodes..."
onos-netcfg localhost ./quancom_qkd_devices.json
sleep 5

echo "Posting fiber links..." 
onos-netcfg localhost ./b5g_links.json

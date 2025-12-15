#!/bin/bash

apt-get update

echo "Waiting for 'quantum-app' to be ready (checking endpoint /onos/quantum-app/nodes/postNode)..."
while [[ "$(curl -s -o /dev/null -w "%{http_code}" -u karaf:karaf 'http://localhost:8181/onos/quantum-app/nodes/postNode')" == "404" ]]; do
	echo "quantum-app not ready yet... Retrying in 5 seconds."
    sleep 5
done
echo "quantum-app ready! Proceeding with the script..."
sleep 5

pip install netconf-console2 six

echo "Configuring etsi qkd EMULATED node identifiers..."
./configure-qkd-nodes.sh
sleep 2


echo "STEP 1: Loading device configuration (quancom_qkd_devices.json)..."
/root/onos/bin/onos-netcfg localhost ./quancom_qkd_devices.json
echo "Waiting 5 seconds for ONOS to process the devices..."
sleep 5

echo "STEP 2: Setting 'QKD' type on devices (via curl)..."
curl -u karaf:karaf -X POST --header 'Content-Type: application/json' --header 'Accept: application/json' 'http://localhost:8181/onos/quantum-app/nodes/postNode?deviceId=netconf%3A172.25.0.101%3A830'
curl -u karaf:karaf -X POST --header 'Content-Type: application/json' --header 'Accept: application/json' 'http://localhost:8181/onos/quantum-app/nodes/postNode?deviceId=netconf%3A172.25.0.102%3A830'
curl -u karaf:karaf -X POST --header 'Content-Type: application/json' --header 'Accept: application/json' 'http://localhost:8181/onos/quantum-app/nodes/postNode?deviceId=netconf%3A172.25.0.103%3A830'
echo "Waiting 2 seconds..."
sleep 2

echo "STEP 3: Loading link configuration (quancom_two_links.json)..."
/root/onos/bin/onos-netcfg localhost ./quancom_two_links.json
echo "Script completed successfully."

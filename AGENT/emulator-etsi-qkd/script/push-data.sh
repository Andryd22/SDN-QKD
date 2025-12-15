#!/bin/bash 

pkill -f sysrepo
pkill -f sysrepod
pkill -f example_application
pkill -f netopeer2-server

export LD_LIBRARY_PATH=/lib:/usr/lib:/usr/local/lib


echo "--- [push-data.sh] Installing Python dependencies..."
pip3 install requests


echo "--- [push-data.sh] installing yang modules ETSI-TYPES..."
sysrepoctl --install /root/yang/etsi-qkd-node-types.yang --owner root --group root --permissions 666
echo "--- [push-data.sh] installing yang modules ETSI-SDN-NODE..."
sysrepoctl --install /root/yang/etsi-qkd-sdn-node.yang --init-data /root/config/init-etsi-qkd-sdn-node.xml --owner root --group root --permissions 666


echo "--- [push-data.sh] starting icton25-driver (Python Server)..."
python3 /root/python_rest/icton25-driver.py &
echo "--- [push-data.sh] waiting for Python Server to bind..."
sleep 3  


echo "--- [push-data.sh] sysrepo subscribe to etsi-qkd-sdn-node changes"
/root/sysrepo/build/examples/application_changes_example etsi-qkd-sdn-node &
sleep 4


echo "--- [push-data.sh] starting netopeer2-server..."
netopeer2-server & 
sleep 5


tail -f /dev/null

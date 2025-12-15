#!/bin/bash

echo "=== Configure emulated QKD nodes" qui serve il comando netconf-console2
netconf-console2 --host=172.25.0.101 --port=830 -u root -p root --rpc=edit-config-node-31.xml
netconf-console2 --host=172.25.0.102 --port=830 -u root -p root --rpc=edit-config-node-32.xml
netconf-console2 --host=172.25.0.103 --port=830 -u root -p root --rpc=edit-config-node-33.xml


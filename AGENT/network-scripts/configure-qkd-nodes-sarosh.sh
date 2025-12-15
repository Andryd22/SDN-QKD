#!/bin/bash

echo "=== Configure emulated QKD nodes" 
/home/alessio/.local/bin/netconf-console2 --host=10.100.101.51 --port=830 -u root -p root --rpc=../emulator-test/edit-config-node-51.xml
/home/alessio/.local/bin/netconf-console2 --host=10.100.101.52 --port=830 -u root -p root --rpc=../emulator-test/edit-config-node-52.xml


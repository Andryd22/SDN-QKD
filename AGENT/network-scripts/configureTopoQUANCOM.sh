#!/bin/bash

echo "=== Configure emulated QKD nodes" 
/home/alessio/.local/bin/netconf-console2 --host=10.100.101.31 --port=830 -u root -p root --rpc=../emulator-test/edit-config-node-31.xml
/home/alessio/.local/bin/netconf-console2 --host=10.100.101.32 --port=830 -u root -p root --rpc=../emulator-test/edit-config-node-32.xml
/home/alessio/.local/bin/netconf-console2 --host=10.100.101.33 --port=830 -u root -p root --rpc=../emulator-test/edit-config-node-33.xml
/home/alessio/.local/bin/netconf-console2 --host=10.100.101.34 --port=830 -u root -p root --rpc=../emulator-test/edit-config-node-34.xml

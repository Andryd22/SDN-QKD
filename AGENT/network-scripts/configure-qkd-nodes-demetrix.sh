#!/bin/bash

echo "=== Configure real QKD nodes" 
/home/alessio/.local/bin/netconf-console2 --host=192.168.1.181  --port=830 -u root -p root --rpc=../emulator-test/edit-config-node-demetrix-181.xml
/home/alessio/.local/bin/netconf-console2 --host=192.168.1.182 --port=830 -u root -p root --rpc=../emulator-test/edit-config-node-demetrix-182.xml

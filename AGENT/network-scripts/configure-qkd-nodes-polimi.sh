#!/bin/bash

echo "=== Configure real QKD nodes" 
/home/alessio/.local/bin/netconf-console2 --host=charlie  --port=11001 -u root -p root --rpc=../emulator-test/edit-config-node-polimi-47.xml
/home/alessio/.local/bin/netconf-console2 --host=alice --port=11001 -u root -p root --rpc=../emulator-test/edit-config-node-polimi-100.xml
/home/alessio/.local/bin/netconf-console2 --host=bob --port=11001 -u root -p root --rpc=../emulator-test/edit-config-node-polimi-118.xml

#!/bin/bash

#Remove previous topology
./removeTopo.sh

echo "Creating QKD nodes..."
echo "--- QKD-1 screen q1 IP 10.100.101.31"
screen -dmS q1 -T xterm sh -c 'docker run --net=netbr0 --ip=10.100.101.31 --name qkd1 -it emulator-etsi:1.0 bash'
sleep 1

echo "--- QKD-2 screen q2 IP 10.100.101.32"
screen -dmS q2 -T xterm sh -c 'docker run --net=netbr0 --ip=10.100.101.32 --name qkd2 -it emulator-etsi:1.0 bash'
sleep 1

#echo "--- QKD-3 screen q3 IP 10.100.101.33"
#screen -dmS q3 -T xterm sh -c 'docker run --net=netbr0 --ip=10.100.101.33 --name qkd3 -it emulator-etsi:1.0 bash'
#sleep 1

#echo "--- QKD-4 screen q4 IP 10.100.101.34"
#screen -dmS q4 -T xterm sh -c 'docker run --net=netbr0 --ip=10.100.101.34 --name qkd4 -it emulator-etsi:1.0 bash'
#sleep 1

echo "--- FIBER-SWITCH-1 screen r1 10.100.101.11"
screen -dmS r1 -T xterm sh -c 'docker run --net=netbr0 --ip=10.100.101.11 --name fiber1 -it tim.it/roadm-9deg:3.0'
sleep 1

echo "--- FIBER-SWITCH-2 screen r2 10.100.101.12"
screen -dmS r2 -T xterm sh -c 'docker run --net=netbr0 --ip=10.100.101.12 --name fiber2 -it tim.it/roadm-9deg:3.0'
sleep 1


echo "Configure QKD datastore" 
sleep 10
netconf-console2 --host=10.100.101.31 --port=830 -u root -p root --rpc=../emulator-test/edit-config-node-31.xml
netconf-console2 --host=10.100.101.32 --port=830 -u root -p root --rpc=../emulator-test/edit-config-node-32.xml

#!/bin/bash

#Remove previous topology
echo "Cleaning docker environment..."
./removeTopo.sh

echo "Creating ROADMs..."
echo "--- ROADM-1 screen r1 IP 10.100.101.11"
screen -dmS "r1" -T xterm sh -c 'docker run --net=netbr0 --ip=10.100.101.11 --name roadm1 -it ornotifelement.img:latest bash'
sleep 2

echo "--- ROADM-2 screen r2 IP 10.100.101.12"
screen -dmS "r2" -T xterm sh -c 'docker run --net=netbr0 --ip=10.100.101.12 --name roadm2 -it ornotifelement.img:latest bash'
sleep 2

echo "--- ROADM-3 screen r3 IP 10.100.101.13"
screen -dmS "r3" -T xterm sh -c 'docker run --net=netbr0 --ip=10.100.101.13 --name roadm3 -it ornotifelement.img:latest bash'
sleep 2

echo "--- ROADM-4 screen r4 IP 10.100.101.14"
screen -dmS "r4" -T xterm sh -c 'docker run --net=netbr0 --ip=10.100.101.14 --name roadm4 -it ornotifelement.img:latest bash'
sleep 2

echo "Creating QKD nodes..."
echo "--- QKD-1 screen q1 IP 10.100.101.31"
screen -dmS "q1" -T xterm sh -c 'docker run --net=netbr0 --ip=10.100.101.31 --name qkd1 -it emulator-etsi:1.0 bash'
sleep 2

echo "--- QKD-2 screen q2 IP 10.100.101.32"
screen -dmS "q2" -T xterm sh -c 'docker run --net=netbr0 --ip=10.100.101.32 --name qkd2 -it emulator-etsi:1.0 bash'
sleep 2

echo "--- QKD-3 screen q3 IP 10.100.101.33"
screen -dmS "q3" -T xterm sh -c 'docker run --net=netbr0 --ip=10.100.101.33 --name qkd3 -it emulator-etsi:1.0 bash'
sleep 2

echo "--- QKD-4 screen q4 IP 10.100.101.34"
screen -dmS "q4" -T xterm sh -c 'docker run --net=netbr0 --ip=10.100.101.34 --name qkd4 -it emulator-etsi:1.0 bash'
sleep 2


echo "Uploading xml schema ../onos-xml/nodeTIM.xml"
sudo docker cp ./nodeTIM.xml roadm1:/confd/examples.confd/OpenROADMNotifElement/
sudo docker cp ./nodeTIM.xml roadm2:/confd/examples.confd/OpenROADMNotifElement/
sudo docker cp ./nodeTIM.xml roadm3:/confd/examples.confd/OpenROADMNotifElement/
sudo docker cp ./nodeTIM.xml roadm4:/confd/examples.confd/OpenROADMNotifElement/
sleep 2

echo "Uploading Makefile"
sudo docker cp ./Makefile-nodeTIM roadm1:/confd/examples.confd/OpenROADMNotifElement/Makefile
sudo docker cp ./Makefile-nodeTIM roadm2:/confd/examples.confd/OpenROADMNotifElement/Makefile
sudo docker cp ./Makefile-nodeTIM roadm3:/confd/examples.confd/OpenROADMNotifElement/Makefile
sudo docker cp ./Makefile-nodeTIM roadm4:/confd/examples.confd/OpenROADMNotifElement/Makefile
sleep 2

echo "Starting the Netconf agents"
sudo screen -S r1 -X stuff './startNetconfAgent.sh\n'
sudo screen -S r2 -X stuff './startNetconfAgent.sh\n'
sudo screen -S r3 -X stuff './startNetconfAgent.sh\n'
sudo screen -S r4 -X stuff './startNetconfAgent.sh\n'

sleep 10



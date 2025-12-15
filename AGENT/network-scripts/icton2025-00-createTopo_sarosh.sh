#!/bin/bash

echo "Creating QKD nodes..."
echo "--- QKD-ALICE screen q5 IP 10.100.101.51"
screen -dmS "q5" -T xterm sh -c 'docker run --net=netbr0 --ip=10.100.101.51 --name qkd5 -it emulator-etsi-test:1.0 bash'
sleep 2

echo "--- QKD-BOB screen q6 IP 10.100.101.52"
screen -dmS "q6" -T xterm sh -c 'docker run --net=netbr0 --ip=10.100.101.52 --name qkd6 -it emulator-etsi-test:1.0 bash'
sleep 2




# Containerize onos controller
The objective of this repository is to containerize the ONOS controller within a Docker environment using a recent Linux distribution, and to demonstrate its functionality through simple applications.
All the source code is taken from the original repository:

- [onos-quancom - By Prof. Alessio Giorgetti](https://github.com/alessiocnit/onos-quancom)


## Author

- Luca Giannini
- Bachelor's Student of Computer Engineering
- University of Pisa, Italy

## Initial Challenge
In recent years, we’ve often heard about quantum computing and quantum physics. This technology could potentially compromise current systems based on classical cryptography.

For this reason, we introduce Quantum Key Distribution (QKD) – a technology capable of distributing cryptographic keys with theoretically unbreakable security.

Although QKD systems already exist, there are challenges when integrating them with today's optical networks. To address this, we introduce Software Defined Networking (SDN), which separates the control plane from the data plane, enabling better flexibility and control.

This work, developed as a bachelor’s thesis, has the following goals:
- Containerize the ONOS controller using Docker
- Test some of the controller's functionalities 


## Tested Environment

This project was tested on a Lenovo IdeaPad 3, running a virtual machine with the following OS: Ubuntu 64-BIT 24.04.2 LTS

## Starting the Process

First you have to clone this repo.

```bash
git clone https://github.com/gianniniluca/Containerize-onos-controller
```


### Download Docker Engine
If you have already installed Docker you can skip to the next one.

To build Docker images and run containers, you must install Docker. 
Start by installing the required packages:
```bash
sudo apt update
sudo apt install \
    ca-certificates \
    curl \
    gnupg \
    lsb-release
```

Then, you can follow these commands:
```bash
sudo mkdir -p /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | \
sudo gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg

echo "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] \
https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" | \
sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

sudo apt update
sudo apt install docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin -y
```

Verify the Docker installation:
```bash
docker --version
```
If you see a Docker version, you're ready to proceed..


## Start with building the images
In order to build the ONOS image you have to make these two steps:
```bash
cd onos-quancom
sudo docker build -t onos-quancom .
```
This may take up to 30 minutes depending on your system..
At the end, if you don't receive warnigs you get an image to run.

 

## Next step is to create network bridge
This command creates a custom bridge network to allow communication between your containers.
```bash
sudo docker network create --driver bridge --subnet 172.25.0.0/16 etsi-net
```


## Starting the ONOS container
Running this commad you create a container with inside ONOS. During the process you can see the logs it helps you to discover potienl error. You have to wait until also the quantum-app have been installed.

```bash
sudo docker run -it --rm --network etsi-net --ip 172.25.0.10  -p 6653:6653   -p 6640:6640   -p 8181:8181   -p 8101:8101   -p 9876:9876 --name onos  onos-quancom
```
## Intecart with ONOS Web GUI

`ONOS GUI is running inside docker: 172.25.0.10:8181` and `ONOS CLI on 8101`. 

The ONOS Web GUI is available at:
http://localhost:8181/onos/ui/login.html

`Credentials to login:`

- username: karaf
- password: karaf


Once logged in, go to Menu → Applications to verify which applications are active.

![ping](/images/gui.png)


## Interact with ONOS CLI

Open a terminal and run:
```bash
ssh -p 8101 karaf@localhost
# password is: karaf
```

This will take you to the `ONOS-CLI`. Here is the example:

![cli](/images/cli.png)


## Test newtowrk bridge

You must load the pre-built ETSI test container image:

```bash
docker load < emulator-etsi-test.tar
```
Check everything go well with this command, you should see emulator-etsi-test image.

```bash
sudo docker images
```
## Starting etsi container

You have to run this image but adding IP addres in order to connect to th network bridge.
```bash
sudo docker run -it --rm --network etsi-net --ip 172.25.0.101 --name etsi1 emulator-etsi-test:1.0
```

You can launch multiple containers by changing the name and IP address accordingly.

## Testing enviromnent
Use the ping command to test communication between ONOS and the ETSI containers
```bash
sudo docker exec onos ping -c 3 172.25.0.101
sudo docker exec etsi1 ping -c 3 172.25.0.10
sudo docker exec etsi1 ping -c 3 172.25.0.102
```
![ping1](/images/ping1.png)
![ping](/images/ping.png)
At this point, you have successfully:

- Containerized ONOS using Docker
- Deployed and interacted with ONOS via GUI and CLI
- Connected ETSI containers using a custom bridge network
- Verified communication between containers using ICMP

## NETCONF Comunication
The purpose of the test is to establish a NETCONF communication between ONOS and the emulated QKD nodes. At the end you can visualize the nodes opening Menu-> Topology

You have to exec this command inside the bash of ONOS.
To use ONOS's bash:
```bash
sudo docker exec -it onos /bin/bash/
```
Then, you can run `./init-network.sh`.
The init-network.sh script is the first to be executed and is responsible for launching all the other secondary scripts involved in the network configuration.
![topology](/images/topology.png)

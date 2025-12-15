#!/bin/sh
#Remove rule for public network
route del -net 131.175.0.0 gw 10.79.250.70 netmask 255.255.0.0 dev gpd0
sleep 1

#Remove rule for class A 10.0.0.0
route del -net 10.0.0.0 gw 10.79.250.70 netmask 255.0.0.0 dev gpd0
sleep 1

#Add rule for class B 10.79.0.0 PoliMi
route add -net 10.79.0.0 gw 10.79.250.70 netmask 255.255.0.0 dev gpd0
sleep 1



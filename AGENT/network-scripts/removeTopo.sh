#!/bin/bash

for i in {1..4}
do
    sudo docker stop -t 1 octp$i
    sudo docker rm octp$i
done
for j in {1..4}
do
    sudo docker stop -t 1 roadm$j
    sudo docker rm roadm$j
done
for j in {1..6}
do
    sudo docker stop -t 1 qkd$j
    sudo docker rm qkd$j
done


#!/bin/bash

#Spenge i container
docker-compose down -v

#Distrugge tutto
rm ~/.ssh/known_hosts

#Ricrea i container
docker-compose up -d 

IP_A=$(docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' node-a)
IP_B=$(docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' node-b)
IP_C=$(docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' node-c)


echo $IP_A $IP_B $IP_C

cat template/config.yaml | sed "s,IP_NODO_A,$IP_A,g" | sed "s,IP_NODO_B,$IP_B,g" | sed "s,IP_NODO_C,$IP_C,g" > config.yaml

cat template/inventory.yaml | sed "s,IP_NODO_A,$IP_A,g" | sed "s,IP_NODO_B,$IP_B,g" | sed "s,IP_NODO_C,$IP_C,g" > inventory.yaml

cat template/client.sh | sed "s,IP_NODO_A,$IP_A,g" | sed "s,IP_NODO_B,$IP_B,g" > client.sh
chmod +x client.sh

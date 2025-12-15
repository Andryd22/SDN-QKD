#!/bin/bash 
  

# Imposta la variabile di app ONOS 

export ONOS_APPS=drivers,gui2,netcfglinksprovider,drivers.odtn-driver,drivers.quantum,optical-rest,roadm   

# Avvia ONOS in background 

/root/onos/bin/onos-service server & 

# Attendi l’avvio (affinabile) 

echo "🕒 Attendo l’avvio di ONOS..." 

sleep 190   

# Installa quantum-app 

#echo "🚀 Installo quantum-app..." 

/root/onos/bin/onos-app localhost install! /root/onos/quantum-app/target/quantum-app-1.0-SNAPSHOT.oar 



#./root/onos/init_network.sh

wait

curl -u karaf:karaf -X POST --header 'Content-Type: application/json' --header 'Accept: application/json' -d '{ 
   "masterAddress": "10.79.1.200", 
   "masterPort": "1234", 
   "slaveAddress": "10.79.1.201", 
   "slavePort": "1234" 
 }' 'http://localhost:8181/onos/quantum-app/keySessions/openKeySession'

echo


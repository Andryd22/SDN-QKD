curl -u karaf:karaf -X POST --header 'Content-Type: application/json' --header 'Accept: application/json' -d '{ 
   "masterAddress": "10.30.2.101", 
   "masterPort": "1234", 
   "slaveAddress": "10.30.2.103", 
   "slavePort": "1234" 
 }' 'http://localhost:8181/onos/quantum-app/keySessions/openKeySession'

echo

curl -u karaf:karaf -X POST --header 'Content-Type: application/json' --header 'Accept: application/json' -d '{ 
   "masterAddress": "10.30.2.102", 
   "masterPort": "1234", 
   "slaveAddress": "10.30.2.103", 
   "slavePort": "1234" 
 }' 'http://localhost:8181/onos/quantum-app/keySessions/openKeySession'

echo

curl -u karaf:karaf -X POST --header 'Content-Type: application/json' --header 'Accept: application/json' -d '{ 
   "masterAddress": "10.30.2.101", 
   "masterPort": "1234", 
   "slaveAddress": "10.30.2.102", 
   "slavePort": "1234" 
 }' 'http://localhost:8181/onos/quantum-app/keySessions/openKeySession'

echo


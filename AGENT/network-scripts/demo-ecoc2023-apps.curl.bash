curl -u karaf:karaf -X POST --header 'Content-Type: application/json' --header 'Accept: application/json' -d '{ 
   "appAddress": "10.30.2.101", 
   "appPort": "1234", 
   "kmAddress": "10.100.101.31", 
   "kmPort": "830" 
 }' 'http://localhost:8181/onos/quantum-app/apps/appRegistration'

echo

curl -u karaf:karaf -X POST --header 'Content-Type: application/json' --header 'Accept: application/json' -d '{ 
   "appAddress": "10.30.2.111", 
   "appPort": "1234", 
   "kmAddress": "10.100.101.31", 
   "kmPort": "830" 
 }' 'http://localhost:8181/onos/quantum-app/apps/appRegistration'

echo

curl -u karaf:karaf -X POST --header 'Content-Type: application/json' --header 'Accept: application/json' -d '{ 
   "appAddress": "10.30.2.102", 
   "appPort": "1234", 
   "kmAddress": "10.100.101.32", 
   "kmPort": "830" 
 }' 'http://localhost:8181/onos/quantum-app/apps/appRegistration'

echo

curl -u karaf:karaf -X POST --header 'Content-Type: application/json' --header 'Accept: application/json' -d '{ 
   "appAddress": "10.30.2.103", 
   "appPort": "1234", 
   "kmAddress": "10.100.101.33", 
   "kmPort": "830" 
 }' 'http://localhost:8181/onos/quantum-app/apps/appRegistration'

echo

curl -u karaf:karaf -X POST --header 'Content-Type: application/json' --header 'Accept: application/json' -d '{ 
   "appAddress": "10.30.2.104", 
   "appPort": "1234", 
   "kmAddress": "10.100.101.34", 
   "kmPort": "830" 
 }' 'http://localhost:8181/onos/quantum-app/apps/appRegistration'

echo

curl -u karaf:karaf -X POST --header 'Content-Type: application/json' --header 'Accept: application/json' -d '{ 
   "appAddress": "10.79.1.101", 
   "appPort": "1234", 
   "kmAddress": "10.79.1.47", 
   "kmPort": "8001" 
 }' 'http://localhost:8181/onos/quantum-app/apps/appRegistration'

echo

curl -u karaf:karaf -X POST --header 'Content-Type: application/json' --header 'Accept: application/json' -d '{ 
   "appAddress": "10.79.1.102", 
   "appPort": "1234", 
   "kmAddress": "10.79.1.47", 
   "kmPort": "8002" 
 }' 'http://localhost:8181/onos/quantum-app/apps/appRegistration'

echo


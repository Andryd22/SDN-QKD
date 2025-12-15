curl -u karaf:karaf -X POST --header 'Content-Type: application/json' --header 'Accept: application/json' -d '{ 
   "appAddress": "10.30.2.201", 
   "appPort": "1234", 
   "kmAddress": "10.100.101.31", 
   "kmPort": "6055" 
 }' 'http://localhost:8181/onos/quantum-app/apps/appRegistration'

echo

curl -u karaf:karaf -X POST --header 'Content-Type: application/json' --header 'Accept: application/json' -d '{ 
   "appAddress": "10.30.2.202", 
   "appPort": "1234", 
   "kmAddress": "10.100.101.32", 
   "kmPort": "6055" 
 }' 'http://localhost:8181/onos/quantum-app/apps/appRegistration'

echo

curl -u karaf:karaf -X POST --header 'Content-Type: application/json' --header 'Accept: application/json' -d '{ 
   "appAddress": "10.30.2.203", 
   "appPort": "1234", 
   "kmAddress": "10.100.101.33", 
   "kmPort": "6055" 
 }' 'http://localhost:8181/onos/quantum-app/apps/appRegistration'

echo

curl -u karaf:karaf -X POST --header 'Content-Type: application/json' --header 'Accept: application/json' -d '{ 
   "appAddress": "10.30.2.204", 
   "appPort": "1234", 
   "kmAddress": "10.100.101.34", 
   "kmPort": "6055" 
 }' 'http://localhost:8181/onos/quantum-app/apps/appRegistration'

echo


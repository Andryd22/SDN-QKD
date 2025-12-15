curl -u karaf:karaf -X POST --header 'Content-Type: application/json' --header 'Accept: application/json' -d '{ 
   "appAddress": "10.79.5.200", 
   "appPort": "1234", 
   "kmAddress": "10.79.5.95", 
   "kmPort": "8443" 
 }' 'http://localhost:8181/onos/quantum-app/apps/appRegistration'

echo

curl -u karaf:karaf -X POST --header 'Content-Type: application/json' --header 'Accept: application/json' -d '{ 
   "appAddress": "10.79.1.200", 
   "appPort": "1234", 
   "kmAddress": "10.79.1.100", 
   "kmPort": "8443" 
 }' 'http://localhost:8181/onos/quantum-app/apps/appRegistration'

echo

curl -u karaf:karaf -X POST --header 'Content-Type: application/json' --header 'Accept: application/json' -d '{ 
   "appAddress": "10.79.1.201", 
   "appPort": "1234", 
   "kmAddress": "10.79.1.229", 
   "kmPort": "8443" 
 }' 'http://localhost:8181/onos/quantum-app/apps/appRegistration'

echo



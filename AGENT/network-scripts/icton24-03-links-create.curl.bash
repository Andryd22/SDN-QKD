echo "PoliMi quantum links..."

curl -u karaf:karaf -X POST --header 'Content-Type: application/json' --header 'Accept: application/json' -d '{ 
   "srcConnectPoint": "netconf:10.79.1.100:11001/1", 
   "dstConnectPoint": "netconf:10.79.5.195:11001/2" 
 }' 'http://localhost:8181/onos/quantum-app/links/createDirectLink'

echo " --- Alice --> Charlie"


curl -u karaf:karaf -X POST --header 'Content-Type: application/json' --header 'Accept: application/json' -d '{ 
   "srcConnectPoint": "netconf:10.79.5.195:11001/1", 
   "dstConnectPoint": "netconf:10.79.1.229:11001/2" 
 }' 'http://localhost:8181/onos/quantum-app/links/createDirectLink'

echo " --- Charlie --> Bob"


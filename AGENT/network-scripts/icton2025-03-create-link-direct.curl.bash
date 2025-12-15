curl -u karaf:karaf -X POST --header 'Content-Type: application/json' --header 'Accept: application/json' -d '{ 
   "srcConnectPoint": "netconf:10.100.101.31:830/1", 
   "dstConnectPoint": "netconf:10.100.101.34:830/1" 
 }' 'http://localhost:8181/onos/quantum-app/links/createLink'

echo

curl -u karaf:karaf -X POST --header 'Content-Type: application/json' --header 'Accept: application/json' -d '{ 
   "srcConnectPoint": "netconf:10.100.101.32:830/1", 
   "dstConnectPoint": "netconf:10.100.101.33:830/1" 
 }' 'http://localhost:8181/onos/quantum-app/links/createLink'

echo


curl -u karaf:karaf -X POST --header 'Content-Type: application/json' --header 'Accept: application/json' 'http://localhost:8181/onos/quantum-app/links/activateLink?key=bbbbbbbb-0047-bbbb-0118-bbbbbbbbbbbb'
echo " --- DIRECT: Charlie 47 --> Bob 118"
sleep 5

curl -u karaf:karaf -X POST --header 'Content-Type: application/json' --header 'Accept: application/json' 'http://localhost:8181/onos/quantum-app/links/activateLink?key=bbbbbbbb-0100-bbbb-0047-bbbbbbbbbbbb'
echo " --- DIRECT: Alice 100 --> Charlie 47"
sleep 5


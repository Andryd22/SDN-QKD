curl -u karaf:karaf -X POST --header 'Content-Type: application/json' --header 'Accept: application/json' 'http://localhost:8181/onos/quantum-app/links/activateLink?key=bbbbbbbb-0047-bbbb-0100-bbbbbbbbbbbb'
echo " --- Charlie 47 --> Alice 100"

curl -u karaf:karaf -X POST --header 'Content-Type: application/json' --header 'Accept: application/json' 'http://localhost:8181/onos/quantum-app/links/activateLink?key=bbbbbbbb-0100-bbbb-0118-bbbbbbbbbbbb'
echo " --- Alice 100 --> Bob 118"

curl -u karaf:karaf -X POST --header 'Content-Type: application/json' --header 'Accept: application/json' 'http://localhost:8181/onos/quantum-app/links/activateLink?key=bbbbbbbb-0118-bbbb-0047-bbbbbbbbbbbb'
echo " --- Bob 118 --> Charlie 47"


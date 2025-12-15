
echo "Deactivating QKD links"

curl -u karaf:karaf -X DELETE --header 'Accept: application/json' 'http://193.205.83.112:8181/onos/quantum-app/links/deactivateLink?key=bbbbbbbb-0047-bbbb-0118-bbbbbbbbbbbb'
echo " --- Charlie 47 --> Bob 118"

curl -u karaf:karaf -X DELETE --header 'Accept: application/json' 'http://193.205.83.112:8181/onos/quantum-app/links/deactivateLink?key=bbbbbbbb-0100-bbbb-0047-bbbbbbbbbbbb'
echo " --- Alice 100 --> Charlie 47"


cd /root/sysrepo/build
cmake -DCMAKE_BUILD_TYPE=Release -DCMAKE_INSTALL_PREFIX:PATH=/usr ..
make -j4
make install 
pkill -f sysrepo
pkill -f sysrepod
sysrepoctl --install /root/yang/etsi-qkd-node-types.yang --owner root --group root --permissions 666
sysrepoctl --install /root/yang/etsi-qkd-sdn-node.yang --init-data /root/config/init-etsi-qkd-sdn-node.xml --owner root --group root --permissions 666
/root/sysrepo/build/examples/application_changes_example etsi-qkd-sdn-node &

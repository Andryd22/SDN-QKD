#! /bin/bash

#post etsi qkd nodes
onos-netcfg localhost ./quancom_qkd_two_devices.json
sleep 5

#post links 
onos-netcfg localhost ./quancom_two_links.json


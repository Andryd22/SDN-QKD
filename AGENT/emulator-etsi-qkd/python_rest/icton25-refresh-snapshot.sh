#!/bin/bash

# Periodically update the snapshot with proper <root> wrapper
while true; do
    {
        echo '<root>';
        sysrepocfg -X -f xml -d running;
        echo '</root>';
    } > /root/python_rest/config_snapshot.xml
    sleep 5
done

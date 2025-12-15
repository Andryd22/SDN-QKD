#!/bin/bash

# Periodically update the snapshot with proper <root> wrapper
    {
        echo '<root>';
        sysrepocfg -X -f xml -d running;
        echo '</root>';
    } > /root/config_snapshot.xml

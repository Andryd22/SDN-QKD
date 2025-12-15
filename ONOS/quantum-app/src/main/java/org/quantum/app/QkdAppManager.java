package org.quantum.app;


import org.onosproject.net.DeviceId;
import org.onosproject.net.device.DeviceService;
import org.onosproject.netconf.*;
import org.osgi.service.component.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Skeletal ONOS application component.
 */
@Component(immediate = true, service = QkdAppManager.class)
public class QkdAppManager {

    private final Logger log = LoggerFactory.getLogger(getClass());

    //Used key is the SAE_ID
    private static final Map<String, QkdApp> qkdAppDatabase = new HashMap<>();
    //Used key is the app_id
    private static final Map<String, QkdKeySession> keySessionDatabase = new HashMap<>();

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected DeviceService deviceService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected NetconfController netconfController;

    private static final String ETSI_TYPE_PREFIX = "xmlns:etsi-qkdn-types=\"urn:etsi:qkd:yang:etsi-qkd-node-types\"";

    @Activate
    protected void activate() {
        log.info("STARTED QkdApp Manager appId");
    }

    @Deactivate
    protected void deactivate() {
        log.info("STOPPED QkdApp Manager appId");
    }

    public void addQkdApp(String key, QkdApp value) {
        qkdAppDatabase.put(key, value);
    }

    public void removeQkdApp(String key) {
        qkdAppDatabase.remove(key);
    }

    public QkdApp getQkdApp(String key) {
        return qkdAppDatabase.get(key);
    }

    public Collection<QkdApp> getQkdApps() {
        return qkdAppDatabase.values();
    }

    public int getDatabaseSize() {
        return qkdAppDatabase.size();
    }

    public QkdApp getQkdApp(String address, String port) {
        for (String key: qkdAppDatabase.keySet()) {
            if ((qkdAppDatabase.get(key).appAddress.equals(address))
                    && (qkdAppDatabase.get(key).appPort.equals(port))) {
                return qkdAppDatabase.get(key);
            }
        }
        return null;
    }
}


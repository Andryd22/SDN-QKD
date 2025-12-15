package org.quantum.app;

import org.onosproject.core.ApplicationId;
import org.onosproject.net.DeviceId;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Component(immediate = true, service = QkdNodeManager.class)
public class QkdNodeManager {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private ApplicationId appId;

     private static final Map<DeviceId, QkdNode> qkdNodeDatabase = new HashMap<>();

    @Activate
    protected void activate() {
        log.info("STARTED QkdNode Manager appId");
    }

    @Deactivate
    protected void deactivate() {
        log.info("STOPPED QkdNode Manager appId");
    }

    public void addQkdNode(DeviceId deviceId, QkdNode value) {
        qkdNodeDatabase.put(deviceId, value);
    }

    public void removeQkdNode(DeviceId deviceId) {
        qkdNodeDatabase.remove(deviceId);
    }

    public QkdNode getQkdNode(DeviceId deviceId) {
        return qkdNodeDatabase.get(deviceId);
    }

    public Collection<QkdNode> getQkdNodes() {
        return qkdNodeDatabase.values();
    }

    public boolean isQkdNode(DeviceId deviceId) {
        for (QkdNode node : qkdNodeDatabase.values()) {
            if (deviceId.equals(node.qkdNodeId)) {
                return true;
            }
        }
        return false;
    }

    public String getKeyManagerId(DeviceId deviceId) {
        return getQkdNode(deviceId).kmId;
    }

    public String getKeyManagerAddressPort(DeviceId deviceId) {
        return getQkdNode(deviceId).kmAddress + ":" + getQkdNode(deviceId).kmPort;
    }

    public int getDatabaseSize() {
        return qkdNodeDatabase.size();
    }
}

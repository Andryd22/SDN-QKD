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
@Component(immediate = true, service = QkdKeySessionManager.class)
public class QkdKeySessionManager {

    private final Logger log = LoggerFactory.getLogger(getClass());

    //Used key is the APP_ID
    private static final Map<String, QkdKeySession> keySessionDatabase = new HashMap<>();

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected NetconfController netconfController;

    private static final String ETSI_TYPE_PREFIX = "xmlns:etsi-qkdn-types=\"urn:etsi:qkd:yang:etsi-qkd-node-types\"";

    @Activate
    protected void activate() {
        log.info("STARTED QkdKeySession Manager appId");
    }

    @Deactivate
    protected void deactivate() {
        log.info("STOPPED QkdKeySession Manager appId");
    }

    public QkdKeySession getKeySession(String key) {
        return keySessionDatabase.get(key);
    }

    public Collection<QkdKeySession> getKeySessions() {
        return keySessionDatabase.values();
    }

    public void addKeySession(String key, QkdKeySession value) {
        keySessionDatabase.put(key, value);
    }

    public void removeKeySession(String key) {
        keySessionDatabase.remove(key);
    }

    public boolean configureKeySession(QkdKeySession keySession) {
        NetconfSession sessionSrc = getNetconfSession(keySession.appMaster.device.id());

        try {
            log.info("REQUEST sent to src QKD node: {}", setAppFilterSource(keySession));
            boolean replySrc = sessionSrc.editConfig(DatastoreId.RUNNING, null, setAppFilterSource(keySession));

            log.info("REPLY to DeviceConfiguration src: {}", replySrc);

        } catch (Exception e) {
            log.error("configureKeySession - Failed configuring source QKD node {}", keySession.appMaster.device.id());
            throw new IllegalStateException(new NetconfException("Failed configuring source QKD node.", e));
        }

        //TODO configure the link_section, list of apps using the specific link

        NetconfSession sessionDst = getNetconfSession(keySession.appSlave.device.id());

        try {
            log.info("REQUEST sent to dst QKD node: {}", setAppFilterDestination(keySession));
            boolean replyDst = sessionDst.editConfig(DatastoreId.RUNNING, null, setAppFilterDestination(keySession));

            log.info("REPLY to DeviceConfiguration dst: {}", replyDst);

        } catch (Exception e) {
            log.error("configureQkdLinks - Failed configuring destination QKD node {}", keySession.appSlave.device.id());
            throw new IllegalStateException(new NetconfException("Failed configuring destination QKD node.", e));
        }

        return true;
    }

    public boolean deleteKeySession(QkdKeySession keySession) {
        NetconfSession sessionSrc = getNetconfSession(keySession.appMaster.device.id());

        try {
            boolean replySrc = sessionSrc.editConfig(DatastoreId.RUNNING, null, deleteAppFilter(keySession));

            log.info("REPLY to DeviceConfiguration src: {}", replySrc);

        } catch (Exception e) {
            log.error("configureKeySession - Failed configuring source QKD node {}", keySession.appMaster.device.id());
            throw new IllegalStateException(new NetconfException("Failed configuring source QKD node.", e));
        }

        //TODO configure the link_section, list of apps using the specific link

        NetconfSession sessionDst = getNetconfSession(keySession.appSlave.device.id());

        try {
            boolean replyDst = sessionDst.editConfig(DatastoreId.RUNNING, null, deleteAppFilter(keySession));

            log.info("REPLY to DeviceConfiguration dst: {}", replyDst);

        } catch (Exception e) {
            log.error("configureQkdLinks - Failed configuring destination QKD node {}", keySession.appSlave.device.id());
            throw new IllegalStateException(new NetconfException("Failed configuring destination QKD node.", e));
        }

        return true;
    }

    private String setAppFilterSource(QkdKeySession session) {
        String appType = "etsi-qkdn-types:CLIENT";

        StringBuilder filter = new StringBuilder();
        filter.append("<qkd_node xmlns=\"urn:etsi:qkd:yang:etsi-qkd-node\">");
        filter.append("  <qkd_applications>");
        filter.append("    <qkd_app>");
        filter.append("      <app_id>" + session.appId + "</app_id>");
        filter.append("      <app_type " + ETSI_TYPE_PREFIX + ">" + appType + "</app_type>");
        filter.append("      <server_app_id>" + session.appMaster.saeId + "</server_app_id>");
        filter.append("      <client_app_id>" + session.appSlave.saeId + "</client_app_id>");
        filter.append("      <app_priority>" + "2" + "</app_priority>");
        filter.append("      <backing_qkdl_id>" + session.linkId + "</backing_qkdl_id>");
        filter.append("      <local_qkdn_id>" + session.appMaster.device.serialNumber() + "</local_qkdn_id>");
        filter.append("      <remote_qkdn_id>" + session.appSlave.device.serialNumber() + "</remote_qkdn_id>");
        filter.append("    </qkd_app>");
        filter.append("  </qkd_applications>");
        filter.append("</qkd_node>");
        return filter.toString();
    }

    private String setAppFilterDestination(QkdKeySession session) {
        String appType = "etsi-qkdn-types:CLIENT";

        StringBuilder filter = new StringBuilder();
        filter.append("<qkd_node xmlns=\"urn:etsi:qkd:yang:etsi-qkd-node\">");
        filter.append("  <qkd_applications>");
        filter.append("    <qkd_app>");
        filter.append("      <app_id>" + session.appId + "</app_id>");
        filter.append("      <app_type " + ETSI_TYPE_PREFIX + ">" + appType + "</app_type>");
        filter.append("      <server_app_id>" + session.appMaster.saeId + "</server_app_id>");
        filter.append("      <client_app_id>" + session.appSlave.saeId + "</client_app_id>");
        filter.append("      <app_priority>" + "2" + "</app_priority>");
        filter.append("      <backing_qkdl_id>" + session.linkId + "</backing_qkdl_id>");
        filter.append("      <local_qkdn_id>" + session.appSlave.device.serialNumber() + "</local_qkdn_id>");
        filter.append("      <remote_qkdn_id>" + session.appMaster.device.serialNumber() + "</remote_qkdn_id>");
        filter.append("    </qkd_app>");
        filter.append("  </qkd_applications>");
        filter.append("</qkd_node>");
        return filter.toString();
    }

    private String deleteAppFilter(QkdKeySession session) {
        StringBuilder filter = new StringBuilder();
        filter.append("<qkd_node xmlns=\"urn:etsi:qkd:yang:etsi-qkd-node\">");
        filter.append("  <qkd_applications>");
        filter.append("    <qkd_app nc:operation=\"delete\">");
        filter.append("      <app_id>" + session.appId + "</app_id>");
        filter.append("    </qkd_app>");
        filter.append("  </qkd_applications>");
        filter.append("</qkd_node>");
        return filter.toString();
    }

    private NetconfSession getNetconfSession(DeviceId deviceId) {
        NetconfDevice netconfDevice = netconfController.getDevicesMap().get(deviceId);
        if (netconfDevice == null) {
            log.error("No netconf device, returning null session");
            return null;
        }
        return netconfDevice.getSession();
    }
}


package org.quantum.app;

import org.apache.commons.configuration.XMLConfiguration;
import org.onlab.util.Frequency;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.drivers.utilities.XmlConfigParser;
import org.onosproject.net.DeviceId;
import org.onosproject.net.OchSignal;
import org.onosproject.net.device.DeviceService;
import org.onosproject.net.intent.IntentService;
import org.onosproject.net.intent.IntentState;
import org.onosproject.netconf.*;
import org.osgi.service.component.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Skeletal ONOS application component.
 */
@Component(immediate = true, service = QkdLinkManager.class)
public class QkdLinkManager {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private ApplicationId appId;
    private static final Map<String, QkdLink> qkdLinkDatabase = new HashMap<>();
    private static final String RPC_TAG_NETCONF_BASE = "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">";
    private static final String RPC_CLOSE_TAG = "</rpc>";
    private static final String ETSI_TYPE_PREFIX = "xmlns:etsi-qkdn-types=\"urn:etsi:qkd:yang:etsi-qkd-node-types\"";

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected NetconfController netconfController;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected DeviceService deviceService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected IntentService intentService;

    @Activate
    protected void activate() {
        log.info("STARTED QkdLink Manager appId");
    }

    @Deactivate
    protected void deactivate() {

        for (QkdLink link : getQkdLinks()) {
            //Only DIRECT LINKS have an associated intent
            if (link.linkType == QkdLink.LinkType.DIRECT) {
                if (intentService.getIntentState(link.intent.get().key()) == IntentState.INSTALLED) {
                    intentService.withdraw(link.intent.get());
                }
            }
        }

        log.info("STOPPED QkdLink Manager appId");
    }

    public void addQkdLink(String key, QkdLink value) {
        qkdLinkDatabase.put(key, value);
    }

    public void removeQkdLink(String key) {
        qkdLinkDatabase.remove(key);
    }

    public QkdLink getQkdLink(String key) {
        log.info("Looking for link {}", key);
        return qkdLinkDatabase.get(key);
    }

    public QkdLink getQkdLink(QkdNode endPoint1, QkdNode endPoint2) {
        for (QkdLink qkdLink : getQkdLinks()) {
            if ((qkdLink.src.deviceId().equals(endPoint1.qkdNodeId)) && (qkdLink.dst.deviceId().equals(endPoint2.qkdNodeId))) {
                return qkdLink;
            }
            if ((qkdLink.src.deviceId().equals(endPoint2.qkdNodeId)) && (qkdLink.dst.deviceId().equals(endPoint1.qkdNodeId))) {
                return qkdLink;
            }
        }
        return null;
    }

    public Collection<QkdLink> getQkdLinks() {
        return qkdLinkDatabase.values();
    }

    public String getQkdDeviceLinks(String key) {
        String reply;
        QkdLink link = getQkdLink(key);

        NetconfSession session = getNetconfSession(link.src.deviceId());

        try {
            reply = session.get(getDeviceDetailsBuilder());
            log.info("REPLY to DeviceDescription {}", reply);

            //Convert string to XML configuration
            XMLConfiguration xconf = (XMLConfiguration) XmlConfigParser.loadXmlString(reply);
        } catch (Exception e) {
            log.error("discoverDeviceDetails - Failed to retrieve session {}", link.src.deviceId());
            throw new IllegalStateException(new NetconfException("Failed to retrieve session.", e));
        }

        return reply;
    }


    public boolean createQkdDirectLink(QkdLink link) {

        NetconfSession sessionSrc = getNetconfSession(link.src.deviceId());

        try {
            log.info("REQUEST sent to src QKD node: {}", createLinkFilterSource(link));
            boolean replySrc = sessionSrc.editConfig(DatastoreId.RUNNING, null, createLinkFilterSource(link));

            log.info("REPLY to DeviceConfiguration src: {}", replySrc);

        } catch (Exception e) {
            log.error("configureQkdLinks - Failed configuring source QKD node {}", link.src.deviceId());
            throw new IllegalStateException(new NetconfException("Failed configuring source QKD node.", e));
        }

        NetconfSession sessionDst = getNetconfSession(link.dst.deviceId());

        try {
            log.info("REQUEST sent to dst QKD node: {}", createLinkFilterDestination(link));
            boolean replyDst = sessionDst.editConfig(DatastoreId.RUNNING, null, createLinkFilterDestination(link));

            log.info("REPLY to DeviceConfiguration dst: {}", replyDst);

        } catch (Exception e) {
            log.error("configureQkdLinks - Failed configuring destination QKD node {}", link.src.deviceId());
            throw new IllegalStateException(new NetconfException("Failed configuring destination QKD node.", e));
        }

        //Confirm the link status
        link.linkStatus = QkdLink.LinkStatus.OFF;

        return true;
    }

    public boolean createQkdVirtualLink(QkdLink link) {

       //TODO to be implemented including NETCONF communication toward devices

        //Confirm the link status
        link.linkStatus = QkdLink.LinkStatus.OFF;

        return true;
    }

    public boolean activateQkdDirectLink(String key, OchSignal signal) {
        QkdLink link = getQkdLink(key);

        if (link.linkType != QkdLink.LinkType.DIRECT) {
            throw new IllegalArgumentException("Link must be of type DIRECT");
        }
        NetconfSession sessionSrc = getNetconfSession(link.src.deviceId());

        try {
            log.info("REQUEST sent to src QKD node: {}", activateLinkFilter(link, signal));
            boolean replySrc = sessionSrc.editConfig(DatastoreId.RUNNING, null, activateLinkFilter(link, signal));

            log.info("REPLY to DeviceConfiguration src: {}", replySrc);

        } catch (Exception e) {
            log.error("deleteQkdLinks - Failed configuring source QKD node {}", link.src.deviceId());
            throw new IllegalStateException(new NetconfException("Failed configuring source QKD node.", e));
        }

        NetconfSession sessionDst = getNetconfSession(link.dst.deviceId());

        try {
            log.info("REQUEST sent to dst QKD node: {}", activateLinkFilter(link, signal));
            boolean replyDst = sessionDst.editConfig(DatastoreId.RUNNING, null, activateLinkFilter(link, signal));

            log.info("REPLY to DeviceConfiguration src: {}", replyDst);

        } catch (Exception e) {
            log.error("deleteQkdLinks - Failed configuring destination QKD node {}", link.src.deviceId());
            throw new IllegalStateException(new NetconfException("Failed configuring destination QKD node.", e));
        }

        //Change the link status
        link.linkStatus = QkdLink.LinkStatus.ACTIVE;

        return true;
    }

    public boolean activateQkdVirtualLink(String key) {
        QkdLink link = getQkdLink(key);

        if (link.linkType != QkdLink.LinkType.VIRTUAL) {
            throw new IllegalArgumentException("Link must be of type VIRTUAL");
        }

        //Check all used links are DIRECT and in status ACTIVE
        for (QkdLink physLink : link.directLinks) {
            if (physLink.linkType != QkdLink.LinkType.DIRECT) {
                throw new IllegalArgumentException("All links used by a VIRTUAL link must be of type DIRECT");
            }

            if (physLink.linkStatus != QkdLink.LinkStatus.ACTIVE) {
                throw new IllegalArgumentException("All links used by a VIRTUAL link must be ACTIVE");
            }
        }

        /*
        TODO: cofigure source node
        NetconfSession sessionSrc = getNetconfSession(link.src.deviceId());

        try {
            log.info("REQUEST sent to src QKD node: {}", activateLinkFilter(link, signal));
            boolean replySrc = sessionSrc.editConfig(DatastoreId.RUNNING, null, activateLinkFilter(link, signal));

            log.info("REPLY to DeviceConfiguration src: {}", replySrc);

        } catch (Exception e) {
            log.error("deleteQkdLinks - Failed configuring source QKD node {}", link.src.deviceId());
            throw new IllegalStateException(new NetconfException("Failed configuring source QKD node.", e));
        }*/

        //TODO: cofigure intermediate nodes

        /*
        TODO: cofigure destination node
        NetconfSession sessionDst = getNetconfSession(link.dst.deviceId());

        try {
            log.info("REQUEST sent to dst QKD node: {}", activateLinkFilter(link, signal));
            boolean replyDst = sessionDst.editConfig(DatastoreId.RUNNING, null, activateLinkFilter(link, signal));

            log.info("REPLY to DeviceConfiguration src: {}", replyDst);

        } catch (Exception e) {
            log.error("deleteQkdLinks - Failed configuring destination QKD node {}", link.src.deviceId());
            throw new IllegalStateException(new NetconfException("Failed configuring destination QKD node.", e));
        }*/

        //Change the link status
        link.linkStatus = QkdLink.LinkStatus.ACTIVE;

        return true;
    }

    public boolean deactivateQkdLink(String key) {
        QkdLink link = getQkdLink(key);

        NetconfSession sessionSrc = getNetconfSession(link.src.deviceId());

        try {
            log.info("REQUEST sent to src QKD node: {}", deactivateLinkFilter(link));
            boolean replySrc = sessionSrc.editConfig(DatastoreId.RUNNING, null, deactivateLinkFilter(link));

            log.info("REPLY to DeviceConfiguration src: {}", replySrc);

        } catch (Exception e) {
            log.error("deleteQkdLinks - Failed configuring source QKD node {}", link.src.deviceId());
            throw new IllegalStateException(new NetconfException("Failed configuring source QKD node.", e));
        }

        NetconfSession sessionDst = getNetconfSession(link.dst.deviceId());

        try {
            log.info("REQUEST sent to dst QKD node: {}", deactivateLinkFilter(link));
            boolean replyDst = sessionDst.editConfig(DatastoreId.RUNNING, null, deactivateLinkFilter(link));

            log.info("REPLY to DeviceConfiguration src: {}", replyDst);

        } catch (Exception e) {
            log.error("deleteQkdLinks - Failed configuring destination QKD node {}", link.src.deviceId());
            throw new IllegalStateException(new NetconfException("Failed configuring destination QKD node.", e));
        }

        //Change the link status
        //TODO consider link to PASSIVE
        link.linkStatus = QkdLink.LinkStatus.OFF;

        return true;
    }

    public boolean deleteQkdLink(String key) {
        QkdLink link = getQkdLink(key);

        NetconfSession sessionSrc = getNetconfSession(link.src.deviceId());

        try {
            log.info("REQUEST sent to src QKD node: {}", deleteLinkFilter(key));
            boolean replySrc = sessionSrc.editConfig(DatastoreId.RUNNING, null, deleteLinkFilter(key));

            log.info("REPLY to DeviceConfiguration src: {}", replySrc);

        } catch (Exception e) {
            log.error("deleteQkdLinks - Failed configuring source QKD node {}", link.src.deviceId());
            throw new IllegalStateException(new NetconfException("Failed configuring source QKD node.", e));
        }

        NetconfSession sessionDst = getNetconfSession(link.dst.deviceId());

        try {
            log.info("REQUEST sent to dst QKD node: {}", deleteLinkFilter(key));
            boolean replyDst = sessionDst.editConfig(DatastoreId.RUNNING, null, deleteLinkFilter(key));

            log.info("REPLY to DeviceConfiguration src: {}", replyDst);

        } catch (Exception e) {
            log.error("deleteQkdLinks - Failed configuring destination QKD node {}", link.src.deviceId());
            throw new IllegalStateException(new NetconfException("Failed configuring destination QKD node.", e));
        }

        return true;
    }

    private NetconfSession getNetconfSession(DeviceId deviceId) {
        NetconfDevice netconfDevice = netconfController.getDevicesMap().get(deviceId);
        if (netconfDevice == null) {
            log.error("No netconf device, returning null session");
            return null;
        }
        return netconfDevice.getSession();
    }

    private String getDeviceDetailsBuilder() {
        StringBuilder filter = new StringBuilder();
        filter.append("<qkd_node xmlns='urn:etsi:qkd:yang:etsi-qkd-node'>");
        filter.append("<qkd_links>");
        filter.append("</qkd_links>");
        filter.append("</qkd_node>");
        return filteredGetBuilder(filter.toString());
    }

    private double freqToLambdaNm(Frequency frequency) {
        int c = 299792458; //Expressed in m/s

        double lambda = c / frequency.asGHz();

        return lambda;
    }

    private String deleteLinkFilter(String key) {
        StringBuilder filter = new StringBuilder();
        filter.append("<qkd_node xmlns=\"urn:etsi:qkd:yang:etsi-qkd-node\">");
        filter.append("  <qkd_links>");
        filter.append("    <qkd_link nc:operation=\"delete\">");
        filter.append("      <qkdl_id>" + key + "</qkdl_id>");
        filter.append("    </qkd_link>");
        filter.append("  </qkd_links>");
        filter.append("</qkd_node>");
        return filter.toString();
    }

    private String activateLinkFilter(QkdLink link, OchSignal signal) {
        StringBuilder filter = new StringBuilder();
        filter.append("<qkd_node xmlns=\"urn:etsi:qkd:yang:etsi-qkd-node\">");
        filter.append("  <qkd_links>");
        filter.append("    <qkd_link>");
        filter.append("      <qkdl_id>" + link.key + "</qkdl_id>");
        filter.append("      <qkdl_status " + ETSI_TYPE_PREFIX + ">" + "etsi-qkdn-types:ACTIVE" + "</qkdl_status>");
        filter.append("      <phys_channel_att>" + String.format("%.2f", link.attenuation) + "</phys_channel_att>");
        filter.append("      <phys_wavelength>" +  signal.spacingMultiplier() + "</phys_wavelength>");
        filter.append("    </qkd_link>");
        filter.append("  </qkd_links>");
        filter.append("</qkd_node>");
        return filter.toString();
    }

    private String deactivateLinkFilter(QkdLink link) {
        StringBuilder filter = new StringBuilder();
        filter.append("<qkd_node xmlns=\"urn:etsi:qkd:yang:etsi-qkd-node\">");
        filter.append("  <qkd_links>");
        filter.append("    <qkd_link>");
        filter.append("      <qkdl_id>" + link.key + "</qkdl_id>");
        //TODO move the status to PASSIVE
        filter.append("      <qkdl_status " + ETSI_TYPE_PREFIX + ">" + "etsi-qkdn-types:OFF" + "</qkdl_status>");
        //filter.append("      <phys_channel_att>" + String.format("%.2f", link.attenuation) + "</phys_channel_att>");
        //filter.append("      <phys_wavelength>" +  signal.spacingMultiplier() + "</phys_wavelength>");
        filter.append("    </qkd_link>");
        filter.append("  </qkd_links>");
        filter.append("</qkd_node>");
        return filter.toString();
    }

    private String createLinkFilterSource(QkdLink link) {
        String localNodeId = deviceService.getDevice(link.src.deviceId()).serialNumber();
        String localPortId = link.src.port().toString();
        String remoteNodeId = deviceService.getDevice(link.dst.deviceId()).serialNumber();
        String remotePortId = link.dst.port().toString();
        String role = "etsi-qkdn-types:TRANSMITTER";

        StringBuilder filter = new StringBuilder();
        filter.append("<qkd_node xmlns=\"urn:etsi:qkd:yang:etsi-qkd-node\">");
        filter.append("  <qkd_links>");
        filter.append("    <qkd_link>");
        filter.append("      <qkdl_id>" + link.key + "</qkdl_id>");
        filter.append("      <qkdl_status " + ETSI_TYPE_PREFIX + ">" + "etsi-qkdn-types:OFF" + "</qkdl_status>");
        filter.append("      <qkdl_enable>true</qkdl_enable>");
        filter.append("      <qkdl_local>");
        filter.append("        <qkdn_id>" + localNodeId + "</qkdn_id>");
        filter.append("        <qkdi_id>" + localPortId + "</qkdi_id>");
        filter.append("      </qkdl_local>");
        filter.append("      <qkdl_remote>");
        filter.append("        <qkdn_id>" + remoteNodeId + "</qkdn_id>");
        filter.append("        <qkdi_id>" + remotePortId + "</qkdi_id>");
        filter.append("      </qkdl_remote>");
        filter.append("      <qkdl_type " + ETSI_TYPE_PREFIX + ">" + "etsi-qkdn-types:PHYS" + "</qkdl_type>");
        //filter.append("      <phys_channel_att>" + String.format("%.2f", link.attenuation) + "</phys_channel_att>");
        //filter.append("      <phys_wavelength>" + "1" + "</phys_wavelength>");
        filter.append("      <phys_qkd_role " + ETSI_TYPE_PREFIX + ">" + role + "</phys_qkd_role>");
        filter.append("    </qkd_link>");
        filter.append("  </qkd_links>");
        filter.append("</qkd_node>");
        return filter.toString();
    }

    private String createLinkFilterDestination(QkdLink link) {

        String localNodeId = deviceService.getDevice(link.dst.deviceId()).serialNumber();
        String localPortId = link.dst.port().toString();
        String remoteNodeId = deviceService.getDevice(link.src.deviceId()).serialNumber();
        String remotePortId = link.src.port().toString();
        String role = "etsi-qkdn-types:RECEIVER";

        StringBuilder filter = new StringBuilder();
        filter.append("<qkd_node xmlns=\"urn:etsi:qkd:yang:etsi-qkd-node\">");
        filter.append("  <qkd_links>");
        filter.append("    <qkd_link>");
        filter.append("      <qkdl_id>" + link.key + "</qkdl_id>");
        filter.append("      <qkdl_status " + ETSI_TYPE_PREFIX + ">" + "etsi-qkdn-types:OFF" + "</qkdl_status>");
        filter.append("      <qkdl_enable>true</qkdl_enable>");
        filter.append("      <qkdl_local>");
        filter.append("        <qkdn_id>" + localNodeId + "</qkdn_id>");
        filter.append("        <qkdi_id>" + localPortId + "</qkdi_id>");
        filter.append("      </qkdl_local>");
        filter.append("      <qkdl_remote>");
        filter.append("        <qkdn_id>" + remoteNodeId + "</qkdn_id>");
        filter.append("        <qkdi_id>" + remotePortId + "</qkdi_id>");
        filter.append("      </qkdl_remote>");
        filter.append("      <qkdl_type " + ETSI_TYPE_PREFIX + ">" + "etsi-qkdn-types:PHYS" + "</qkdl_type>");
        //filter.append("      <phys_channel_att>" + String.format("%.2f", link.attenuation) + "</phys_channel_att>");
        //filter.append("      <phys_wavelength>" + "1" + "</phys_wavelength>");
        filter.append("      <phys_qkd_role " + ETSI_TYPE_PREFIX + ">" + role + "</phys_qkd_role>");
        filter.append("    </qkd_link>");
        filter.append("  </qkd_links>");
        filter.append("</qkd_node>");
        return filter.toString();
    }

    /**
     * Construct a String with a Netconf filtered get RPC Message.
     *
     * @param filter A valid XML tree with the filter to apply in the get
     * @return a String containing the RPC XML Document
     */
    private String filteredGetBuilder(String filter) {
        StringBuilder rpc = new StringBuilder(RPC_TAG_NETCONF_BASE);
        rpc.append("<get>");
        rpc.append("<filter type='subtree'>");
        rpc.append(filter);
        rpc.append("</filter>");
        rpc.append("</get>");
        rpc.append(RPC_CLOSE_TAG);
        return rpc.toString();
    }

    private String filteredEditBuilder(String filter) {
        StringBuilder rpc = new StringBuilder(RPC_TAG_NETCONF_BASE);
        rpc.append("<get>");
        rpc.append("<filter type='subtree'>");
        rpc.append(filter);
        rpc.append("</filter>");
        rpc.append("</get>");
        rpc.append(RPC_CLOSE_TAG);
        return rpc.toString();
    }
    public int getDatabaseSize() {
        return qkdLinkDatabase.size();
    }
}



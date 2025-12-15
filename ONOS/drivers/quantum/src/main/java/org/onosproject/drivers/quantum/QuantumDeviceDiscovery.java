package org.onosproject.drivers.quantum;

import com.google.common.collect.ImmutableList;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.onlab.packet.ChassisId;
import org.onosproject.drivers.utilities.XmlConfigParser;
import org.onosproject.net.*;
import org.onosproject.net.device.*;
import org.onosproject.net.driver.AbstractHandlerBehaviour;
import org.onosproject.net.optical.device.OchPortHelper;
import org.onosproject.net.optical.impl.DefaultOchPort;
import org.onosproject.netconf.NetconfController;
import org.onosproject.netconf.NetconfDevice;
import org.onosproject.netconf.NetconfException;
import org.onosproject.netconf.NetconfSession;
import org.slf4j.Logger;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static org.onosproject.ui.GlyphConstants.CHAIN;
import static org.onosproject.ui.GlyphConstants.LOCK;
import static org.slf4j.LoggerFactory.getLogger;

public class QuantumDeviceDiscovery extends AbstractHandlerBehaviour
        implements DeviceDescriptionDiscovery {
    private final Logger log = getLogger(getClass());

    private static final String RPC_TAG_NETCONF_BASE =
            "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">";

    private static final String RPC_CLOSE_TAG = "</rpc>";

    /**
     * Discovers device details, for etsi-015 device by getting the system information.
     *
     * @return device description
     */
    @Override
    public DeviceDescription discoverDeviceDetails() {
        log.info("Discovering QUANTUM device details...");

        // Some defaults
        String vendor       = "NOVENDOR";
        String hwVersion    = "NOHWVERSION";
        String swVersion    = "NOSWVERSION";
        String serialNumber = "NOSERIAL";
        String chassisId    = "NOCHASSISID";

        // Get the session,
        NetconfSession session = getNetconfSession(did());
        try {
            String reply = session.get(getDeviceDetailsBuilder());
            log.info("REPLY to DeviceDescription {}", reply);

            // <rpc-reply> as root node, software hardare version requires openconfig >= 2018
            XMLConfiguration xconf = (XMLConfiguration) XmlConfigParser.loadXmlString(reply);
            serialNumber = xconf.getString("data.qkd_node.qkdn_id", serialNumber);
            swVersion    = xconf.getString("data.qkd_node.qkdn_version", swVersion);
            hwVersion    = xconf.getString("data.qkd_node.qkdn_version", hwVersion);
        } catch (Exception e) {
            log.error("discoverDeviceDetails - Failed to retrieve session {}", did());
            throw new IllegalStateException(new NetconfException("Failed to retrieve session.", e));
        }

        DefaultAnnotations annotations = DefaultAnnotations.builder()
                .set(AnnotationKeys.UI_TYPE, CHAIN)
                .build();

        DefaultDeviceDescription description = new DefaultDeviceDescription(
                did().uri(),
                Device.Type.TERMINAL_DEVICE,
                "PoliMI-QUANCOM",
                hwVersion,
                swVersion,
                serialNumber,
                new ChassisId(10),
                true,
                annotations
        );

        return description;
    }
    /**
     * Discovers port details, for polatis device.
     *
     * @return port list
     */
    @Override
    public List<PortDescription> discoverPortDetails() {
        log.info("Discovering ports on QUANTUM device...");

        NetconfSession session = getNetconfSession(did());
        try {
            String reply = session.get(getDeviceDetailsBuilder());
            log.info("REPLY to discoverPortDetails {}", reply);

            // <rpc-reply> as root node, software hardare version requires openconfig >= 2018
            XMLConfiguration xconf = (XMLConfiguration) XmlConfigParser.loadXmlString(reply);
            HierarchicalConfiguration interfaces = xconf.configurationAt("data.qkd_node.qkd_interfaces");

            return parsePorts(interfaces);
        } catch (Exception e) {
            log.error("discoverPortDetails - Failed to retrieve session {}", did());
            throw new IllegalStateException(new NetconfException("Failed to retrieve session.", e));
        }

    }

    private List<PortDescription> parsePorts(HierarchicalConfiguration interfacesXml) {
        List<PortDescription> descriptions = new ArrayList<>();

        List<HierarchicalConfiguration> interfaces = interfacesXml.configurationsAt("qkd_interface");
        log.info("Number of retrieved ports {}", interfaces.size());

        for (HierarchicalConfiguration interfaceConfig : interfaces) {
            log.info("Parsing device port {}", interfaceConfig);

            DefaultAnnotations annotations = DefaultAnnotations.builder()
                    .set("qkdi_id", interfaceConfig.getString("qkdi_id"))
                    .set("qkdi_model", interfaceConfig.getString("qkdi_model"))
                    .set("qkdi_type", interfaceConfig.getString("qkdi_type"))
                    .set("qkdi_status", interfaceConfig.getString("qkdi_status", "none"))
                    .set("qkdi_att_point.device", interfaceConfig.getString("qkdi_att_point.device"))
                    .set("qkdi_att_point.port", interfaceConfig.getString("qkdi_att_point.port"))
                    .set("qkdi_capabilities.max_absorption", interfaceConfig.getString("qkdi_capabilities.max_absorption","none"))
                    .set("qkdi_capabilities.role_support", interfaceConfig.getString("qkdi_capabilities.role_support"))
                    .set("qkdi_capabilities.wavelength_range", interfaceConfig.getString("qkdi_capabilities.wavelength_range"))
                    .build();

            log.info("Parsing device annotation {}", annotations);

            PortDescription portDescription = OchPortHelper.ochPortDescription(
                    PortNumber.portNumber(Integer.valueOf(interfaceConfig.getString("qkdi_id"))),
                    true,
                    OduSignalType.ODU4,
                    true,
                    OchSignal.newDwdmSlot(ChannelSpacing.CHL_50GHZ, 1),
                    annotations);

            descriptions.add(portDescription);
        }
        return descriptions;
    }

    /**
     * Get the deviceId for which the methods apply.
     *
     * @return The deviceId as contained in the handler data
     */
    private DeviceId did() {
        return handler().data().deviceId();
    }

    /**
     * Returns the NetconfSession with the device for which the method was called.
     *
     * @param deviceId device indetifier
     *
     * @return The netconf session or null
     */
    private NetconfSession getNetconfSession(DeviceId deviceId) {
        NetconfController controller = handler().get(NetconfController.class);
        NetconfDevice ncdev = controller.getDevicesMap().get(deviceId);
        if (ncdev == null) {
            log.trace("No netconf device, returning null session");
            return null;
        }
        return ncdev.getSession();
    }

    /**
     * Builds a request to get Device details, operational data.
     *
     * @return A string with the Netconf RPC for a get with subtree rpcing based on
     *    /components/component/state/type being oc-platform-types:OPERATING_SYSTEM
     */
    private String getDeviceDetailsBuilder() {
        StringBuilder filter = new StringBuilder();
        filter.append("<qkd_node xmlns='urn:etsi:qkd:yang:etsi-qkd-node'>");
        filter.append("</qkd_node>");
        return filteredGetBuilder(filter.toString());
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
}

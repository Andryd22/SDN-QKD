package org.quantum.app;

import org.onosproject.net.ConnectPoint;
import org.onosproject.net.Link;
import org.onosproject.net.OchSignal;
import org.onosproject.net.device.DeviceService;
import org.onosproject.net.intent.Intent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.onosproject.cli.AbstractShellCommand.get;

public class QkdLink {
    private final Logger log = LoggerFactory.getLogger(getClass());

    protected String key;
    protected ConnectPoint src;
    protected QkdNode qkdSrc;
    protected ConnectPoint dst;
    protected QkdNode qkdDst;
    protected double attenuation;
    protected Optional<Intent> intent;

    protected enum LinkStatus {ACTIVE, PASSIVE, PENDING, OFF}
    protected LinkStatus linkStatus;
    protected enum LinkType {DIRECT, VIRTUAL}
    protected LinkType linkType;
    protected List<QkdLink> directLinks = new ArrayList<>(); // Used only for Virtual links

    protected double creationTime;
    protected double activationTime;

    QkdNodeManager nodeManager = get(QkdNodeManager.class);

    public QkdLink(ConnectPoint srcCP, ConnectPoint dstCP, LinkType type, double att, Intent in) {
        DeviceService deviceService = get(DeviceService.class);

        log.info("serial source {}", deviceService.getDevice(srcCP.deviceId()).serialNumber());
        log.info("serial destination {}", deviceService.getDevice(dstCP.deviceId()).serialNumber());

        String source = deviceService.getDevice(srcCP.deviceId()).serialNumber().split("-")[3];
        String destination = deviceService.getDevice(dstCP.deviceId()).serialNumber().split("-")[3];

        key = "bbbbbbbb-" + source + "-bbbb-" + destination + "-bbbbbbbbbbbb";

        src = srcCP;
        qkdSrc = nodeManager.getQkdNode(srcCP.deviceId());
        dst = dstCP;
        qkdDst = nodeManager.getQkdNode(dstCP.deviceId());
        linkType = type;
        attenuation = att;

        creationTime = 0;
        activationTime = 0;

        //Virtual links do not include an intent
        if (in != null) {
            intent = Optional.of(in);
        } else {
            intent = Optional.empty();
        }

        linkStatus = LinkStatus.OFF;
    }
}

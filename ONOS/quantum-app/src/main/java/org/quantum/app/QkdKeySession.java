package org.quantum.app;

import org.onlab.util.ItemNotFoundException;
import org.onosproject.net.device.DeviceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.onosproject.cli.AbstractShellCommand.get;

public class QkdKeySession {

    private final Logger log = LoggerFactory.getLogger(getClass());

    QkdKeySessionManager sessionManager = get(QkdKeySessionManager.class);

    QkdLinkManager linkManager = get(QkdLinkManager.class);

    //These parameters are loaded from REST
    protected QkdApp appMaster;
    protected QkdApp appSlave;
    protected String appId;
    protected String linkId;

    //TODO define a field QoS

    public QkdKeySession(QkdApp master, QkdApp slave) {

        appMaster = master;
        appSlave = slave;

        String prefix = master.saeId.split("-")[3];
        String suffix = slave.saeId.split("-")[3];

        appId = "dddddddd-" + prefix + "-dddd-" + suffix + "-dddddddddddd";

        if (sessionManager.getKeySession(appId) != null) {
            throw new ItemNotFoundException("Key session is already known");
        }

        //Find a qkd link suitable for this session
        QkdLink qkdLink = linkManager.getQkdLink(master.qkdNode, slave.qkdNode);

        if (qkdLink == null) {
            throw new ItemNotFoundException("A link for this session does not exist");
        }

        linkId = qkdLink.key;

        //String source = master.device.serialNumber().split("-")[3];
        //String destination = slave.device.serialNumber().split("-")[3];
        //linkId = "bbbbbbbb-" + source + "-bbbb-" + destination + "-bbbbbbbbbbbb";
    }
}

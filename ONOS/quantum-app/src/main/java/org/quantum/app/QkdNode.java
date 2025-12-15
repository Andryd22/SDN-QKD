package org.quantum.app;

import org.onlab.util.ItemNotFoundException;
import org.onosproject.net.Device;
import org.onosproject.net.DeviceId;
import org.onosproject.net.device.DeviceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.onosproject.cli.AbstractShellCommand.get;

public class QkdNode {
    private final Logger log = LoggerFactory.getLogger(getClass());
    DeviceId qkdNodeId;

    protected String etsi015Serial;
    protected String kmAddress;
    protected String kmPort;
    protected String kmId;

    QkdNodeManager manager = get(QkdNodeManager.class);

    DeviceService deviceService = get(DeviceService.class);

    public QkdNode(Device device) {

        qkdNodeId = device.id();

        etsi015Serial = device.serialNumber();

        kmAddress = device.id().toString().split(":")[1];
        kmPort =  Integer.toString(Integer.parseInt(etsi015Serial.split("-")[4]));
        kmId = etsi015Serial;

        //Add the app to the local database
        manager.addQkdNode(qkdNodeId, this);

        log.info("A new QkdNode added {} with key manager id", device.id(), kmId);
    }
}
package org.quantum.app;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.onlab.util.ItemNotFoundException;
import org.onosproject.net.Device;
import org.onosproject.net.DeviceId;
import org.onosproject.net.device.DeviceService;
import org.onosproject.rest.AbstractWebResource;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * QKD nodes control APIs.
 */
@Path("nodes")
public class QkdNodeWebResource extends AbstractWebResource {
    QkdNodeManager nodeManager = get(QkdNodeManager.class);

    DeviceService deviceService = get(DeviceService.class);

    /**
     * Get QKD Nodes.
     *
     * @return 200 OK
     */
    @GET
    @Path("getNodes")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getQkdNodes() {

        ArrayNode nodes = mapper().createArrayNode();

        for (QkdNode qkdNode: nodeManager.getQkdNodes()) {
            ObjectNode node = mapper().createObjectNode()
                    .put("deviceId", qkdNode.qkdNodeId.toString())
                    .put("kmAddress", qkdNode.kmAddress)
                    .put("kmPort", qkdNode.kmPort)
                    .put("kmId", qkdNode.kmId)
                    .put("serial", qkdNode.etsi015Serial);

            nodes.add(node);
        }

        ObjectNode root = this.mapper().createObjectNode().putPOJO("QkdNodes", nodes);
        return ok(root).build();
    }

    /**
     * Post a QKD node.
     *
     * @param deviceId a device already connected to controller
     * @return 200 OK
     */
    @POST
    @Path("postNode")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response postNode(@QueryParam("deviceId") String deviceId) {
        QkdNode qkdNode;

        Device device = deviceService.getDevice(DeviceId.deviceId(deviceId));
        if (device == null) {
            throw new IllegalArgumentException("Specified device does not exist");
        }

        if (!device.manufacturer().equals("PoliMI-QUANCOM")) {
            throw new ItemNotFoundException("Specified device is of wrong type.");
        }

        if (nodeManager.getQkdNode(device.id()) != null) {
            throw new ItemNotFoundException("Specified device is already registered.");
        }

        try {
            qkdNode = new QkdNode(device);
        } catch (ItemNotFoundException ioe) {
            throw new IllegalArgumentException(ioe);
        }

        ObjectNode node = mapper().createObjectNode()
                .put("serial", qkdNode.etsi015Serial);

        return Response.ok(node).build();
    }
}

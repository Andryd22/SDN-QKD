package org.quantum.app;


import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.net.ConnectPoint;
import org.onosproject.net.DefaultPath;
import org.onosproject.net.OchSignal;
import org.onosproject.net.device.DeviceService;
import org.onosproject.net.flow.criteria.Criterion;
import org.onosproject.net.flow.criteria.OchSignalCriterion;
import org.onosproject.net.intent.*;
import org.onosproject.net.optical.util.OpticalIntentUtility;
import org.onosproject.rest.AbstractWebResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Path;
import javax.ws.rs.POST;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.onlab.util.Tools.nullIsNotFound;
import static org.onlab.util.Tools.readTreeFromStream;

/**
 * Quantum links control APIs.
 */
@Path("links")
public class QkdLinkWebResource extends AbstractWebResource {
    private final Logger log = LoggerFactory.getLogger(getClass());

    IntentService intentService = get(IntentService.class);
    DeviceService deviceService = get(DeviceService.class);
    CoreService coreService = get(CoreService.class);
    QkdLinkManager linkManager = get(QkdLinkManager.class);
    QkdNodeManager nodeManager = get(QkdNodeManager.class);

    /**
     * Get established QKD links.
     *
     * @return 200 OK
     */
    @GET
    @Path("getLinks")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getQkdLinks() {

        ArrayNode links = mapper().createArrayNode();

        for (QkdLink link: linkManager.getQkdLinks()) {
            ObjectNode node = mapper().createObjectNode()
                    .put("src", link.src.toString())
                    .put("dst", link.dst.toString())
                    .put("key", link.key)
                    .put("status", link.linkStatus.toString())
                    .put("type", link.linkType.toString())
                    .put("creationTime", link.creationTime)
                    .put("activationTime", link.activationTime);

            links.add(node);
        }

        ObjectNode root = this.mapper().createObjectNode().putPOJO("QkdLinks", links);
        return ok(root).build();
    }

    /**
     * Get established QKD links.
     *
     * @return 200 OK
     */
    @GET
    @Path("getLink")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getQkdLink(@QueryParam("key") String key) {

        if (linkManager.getQkdLink(key) == null) {
            throw new IllegalArgumentException("Specified link does not exist");
        }

        QkdLink link = linkManager.getQkdLink(key);

        ObjectNode node = mapper().createObjectNode()
                .put("src", link.src.toString())
                .put("dst", link.dst.toString())
                .put("key", link.key)
                .put("status", link.linkStatus.toString())
                .put("type", link.linkType.toString());

        //Information specific of DIRECT links
        if (link.linkType.equals(QkdLink.LinkType.DIRECT)) {
            node.put("intent-id", link.intent.get().id().toString())
                    .put("srcKmeId", link.qkdSrc.kmId.toString())
                    .put("srcKmeAddress", link.qkdSrc.kmAddress.toString())
                    .put("srcKmePort", link.qkdSrc.kmPort.toString())
                    .put("dstKmeId", link.qkdDst.kmId.toString())
                    .put("dstKmeAddress", link.qkdDst.kmAddress.toString())
                    .put("dstKmePort", link.qkdDst.kmPort.toString());
        }

        //Information specific of VIRTUAL links
        if (link.linkType.equals(QkdLink.LinkType.VIRTUAL)) {
            ArrayNode directLinks = mapper().createArrayNode();

            for (QkdLink directLink: link.directLinks) {
                ObjectNode directLinkNode = mapper().createObjectNode()
                        .put("key", directLink.key.toString())
                        .put("srcKmeId", directLink.qkdSrc.kmId.toString())
                        .put("srcKmeAddress", directLink.qkdSrc.kmAddress.toString())
                        .put("srcKmePort", directLink.qkdSrc.kmPort.toString())
                        .put("dstKmeId", directLink.qkdDst.kmId.toString())
                        .put("dstKmeAddress", directLink.qkdDst.kmAddress.toString())
                        .put("dstKmePort", directLink.qkdDst.kmPort.toString());

                directLinks.add(directLinkNode);
            }

            node.putPOJO("directLinks", directLinks);
        }

        return ok(node).build();
    }

    /**
     * Create a new QKD link with status OFF.
     *
     * @return 200 ok
     * @onos.rsModel linkCreate
     */
    @POST
    @Path("createDirectLink")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createQkdPhysicalLink(InputStream stream) throws InterruptedException {
        String srcConnectPoint, dstConnectPoint;

        long startTime = System.currentTimeMillis();
        long elapsedTime = 0;

        log.info("New DIRECT QkdLink has been requested");

        try {
            ObjectNode root = readTreeFromStream(mapper(), stream);

            srcConnectPoint = root.get("srcConnectPoint").asText();
            dstConnectPoint = root.get("dstConnectPoint").asText();

            if ((srcConnectPoint.equals(dstConnectPoint))) {
                throw new IllegalArgumentException("Source and destination QKD nodes must be different");
            }

        } catch (IOException ioe) {
            throw new IllegalArgumentException(ioe);
        }

        ConnectPoint ingress = ConnectPoint.fromString(srcConnectPoint);
        ConnectPoint egress = ConnectPoint.fromString(dstConnectPoint);

        log.info("New DIRECT QkdLink has been requested from {} to {}", ingress.deviceId(), egress.deviceId());

        if (deviceService.getDevice(ingress.deviceId()) == null) {
            throw new IllegalArgumentException("Source node does not exist");
        }

        if (deviceService.getDevice(egress.deviceId()) == null) {
            throw new IllegalArgumentException("Destination node does not exist");
        }

        Key key = null;
        ApplicationId appId = nullIsNotFound(coreService.getAppId("org.quantum.app"), "App ID not found");
        boolean bidirectional = false;
        OchSignal signal = null;
        DefaultPath suggestedPath = null;

        //Ask a connectivity intent
        Intent intent = OpticalIntentUtility.createExplicitOpticalIntent(
                ingress,
                egress,
                deviceService,
                key,
                appId,
                bidirectional,
                signal,
                suggestedPath);

        //Configure src and dst QKD
        QkdLink qkdLink = new QkdLink(
                ingress,
                egress,
                QkdLink.LinkType.DIRECT,
                5.0,
                intent);

        //Check such link already exist
        if (linkManager.getQkdLink(qkdLink.key) != null) {
            throw new IllegalArgumentException(
                    "Such link already exists in status " + linkManager.getQkdLink(qkdLink.key).linkStatus.toString());
        }

        //Configure link section of nodes
        linkManager.createQkdDirectLink(qkdLink);

        //Add to local database
        linkManager.addQkdLink(qkdLink.key, qkdLink);

        elapsedTime = System.currentTimeMillis() - startTime;
        log.info("New DIRECT QkdLink has been created {}", qkdLink.key);
        qkdLink.creationTime = elapsedTime;

        ObjectNode node = mapper().createObjectNode()
                .put("link_id", qkdLink.key);

        return Response.ok(node).build();
    }

    /**
     * Create a new QKD link with status OFF.
     *
     * @return 200 ok
     * @onos.rsModel linkCreate
     */
    @POST
    @Path("createVirtualLink")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createQkdVirtualLink(InputStream stream) throws InterruptedException {
        String srcConnectPoint, dstConnectPoint;

        try {
            ObjectNode root = readTreeFromStream(mapper(), stream);

            srcConnectPoint = root.get("srcConnectPoint").asText();
            dstConnectPoint = root.get("dstConnectPoint").asText();

            if ((srcConnectPoint.equals(dstConnectPoint))) {
                throw new IllegalArgumentException("Source and destination QKD nodes must be different");
            }

        } catch (IOException ioe) {
            throw new IllegalArgumentException(ioe);
        }

        ConnectPoint ingress = ConnectPoint.fromString(srcConnectPoint);
        ConnectPoint egress = ConnectPoint.fromString(dstConnectPoint);

        if (deviceService.getDevice(ingress.deviceId()) == null) {
            throw new IllegalArgumentException("Source node does not exist");
        }

        if (deviceService.getDevice(egress.deviceId()) == null) {
            throw new IllegalArgumentException("Destination node does not exist");
        }

        //Configure src and dst QKD
        QkdLink qkdLink = new QkdLink(
                ingress,
                egress,
                QkdLink.LinkType.VIRTUAL,
                5.0,
                null);

        //TODO da sistemare, qui prende tutto cablato
        qkdLink.directLinks.add(linkManager.getQkdLink("bbbbbbbb-0100-bbbb-0047-bbbbbbbbbbbb"));
        qkdLink.directLinks.add(linkManager.getQkdLink("bbbbbbbb-0047-bbbb-0118-bbbbbbbbbbbb"));

        //Check such link already exist
        if (linkManager.getQkdLink(qkdLink.key) != null) {
            throw new IllegalArgumentException(
                    "Such link already exists in status " + linkManager.getQkdLink(qkdLink.key).linkStatus.toString());
        }

        //Configure link section of nodes
        linkManager.createQkdVirtualLink(qkdLink);

        //Add to local database
        linkManager.addQkdLink(qkdLink.key, qkdLink);

        log.info("New VIRTUAL QkdLink has been created {}", qkdLink.key);

        ObjectNode node = mapper().createObjectNode()
                .put("link_id", qkdLink.key);

        return Response.ok(node).build();
    }

    /**
     * Activate a currently OFF QKD link.
     *
     * @param key link_id of the quantum channel
     * @return
     */
    @POST
    @Path("activateLink")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response activateQkdLink(@QueryParam("key") String key)
            throws InterruptedException {

        QkdLink qkdLink = linkManager.getQkdLink(key);

        if (qkdLink == null) {
            throw new IllegalArgumentException("Quantum link does not exist");
        }

        if (qkdLink.linkStatus.equals(QkdLink.LinkStatus.ACTIVE)) {
            throw new IllegalArgumentException("Quantum link is already ACTIVE");
        }

        if (qkdLink.linkType.equals(QkdLink.LinkType.VIRTUAL)) {

            linkManager.activateQkdVirtualLink(qkdLink.key);
        }

        if (qkdLink.linkType.equals(QkdLink.LinkType.DIRECT)) {
            intentService.submit(qkdLink.intent.get());

            long startTime = System.currentTimeMillis();
            long elapsedTime = 0;

            log.info("The ACTIVATION of a DIRECT QkdLink has been requested id {}", key);

            while (intentService.getIntentState(qkdLink.intent.get().key()) != IntentState.INSTALLED) {
                //Thread.sleep(10);
                elapsedTime = System.currentTimeMillis() - startTime;
                //log.info("Waiting for optical intent installation delay {} ms", elapsedTime);

                if (elapsedTime > 10000) {
                    break;
                }
            }

            elapsedTime = System.currentTimeMillis() - startTime;
            qkdLink.activationTime = elapsedTime;
            log.info("The ACTIVATION of a DIRECT QkdLink has been done id {} delay {} ms", key, elapsedTime);

            //Uses selected lambda from intent ochsignal
            linkManager.activateQkdDirectLink(qkdLink.key, getOchSignal(qkdLink.intent.get()));
        }

        log.info("A QkdLink has been activated {} type {}", qkdLink.key, qkdLink.linkType);

        ObjectNode node = mapper().createObjectNode()
                .put("activated link_id", qkdLink.key);

        return Response.ok(node).build();
    }

    /**
     * De-activate a currently ACTIVE QKD link.
     *
     * @param key link_id of the quantum channel
     * @return
     */
    @DELETE
    @Path("deactivateLink")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response deactivateQkdLink(@QueryParam("key") String key)
            throws InterruptedException {

        QkdLink qkdLink = linkManager.getQkdLink(key);

        if (qkdLink.linkStatus.equals(QkdLink.LinkStatus.OFF)) {
            throw new IllegalArgumentException("Quantum link is already OFF");
        }

        if (qkdLink.linkStatus.equals(QkdLink.LinkStatus.PASSIVE)) {
            throw new IllegalArgumentException("Quantum link is already PASSIVE");
        }

        //Uses selected lambda from intent ochsignal
        linkManager.deactivateQkdLink(qkdLink.key);

        //TODO should be status PASSIVE
        log.info("A QkdLink has been deactivated {} and is now in state OFF", qkdLink.key);

        intentService.withdraw(qkdLink.intent.get());

        ObjectNode node = mapper().createObjectNode()
                .put("deactivated link_id", qkdLink.key);

        return Response.ok(node).build();
    }

    /**
     * Delete a QKD link.
     *
     * @param key link_id of the quantum channel
     * @return 200 OK
     */
    @DELETE
    @Path("deleteLink")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteQkdLink(@QueryParam("key") String key) {

        QkdLink link = linkManager.getQkdLink(key);

        if (link == null) {
            throw new IllegalArgumentException("Specified qkd link does not exist");
        }

        //Delete node configuration
        linkManager.deleteQkdLink(key);

        //Remove from database
        linkManager.removeQkdLink(key);

        //Remove optical intent
        intentService.withdraw(link.intent.get());

        return Response.ok().build();
    }

    /**
     * Set the link status.
     *
     * @param @param key app_id of the session
     * @param status enum from ETSI 015
     * @return 200 OK
     */
    @POST
    @Path("setKeySessionStatus")
    public Response setSessionStatus(@QueryParam("key") String key,
                                     @QueryParam("status") String status) {

        QkdLink link = linkManager.getQkdLink(key);

        switch (status) {
            case "OFF":
                link.linkStatus = QkdLink.LinkStatus.OFF;
                break;
            case "PASSIVE":
                link.linkStatus = QkdLink.LinkStatus.PASSIVE;
                break;
            case "ACTIVE":
                link.linkStatus = QkdLink.LinkStatus.ACTIVE;
                break;
            case "PENDING":
                link.linkStatus = QkdLink.LinkStatus.PENDING;
                break;
            default:
                throw new IllegalArgumentException("Status not valid, allowed OFF/PASSIVE/ACTIVE/PENDING");
        }

        return Response.ok().build();
    }

    private OchSignal getOchSignal(Intent intent) {

        FlowRuleIntent installableIntent =
                (FlowRuleIntent) intentService.getInstallableIntents(intent.key())
                        .stream()
                        .filter(FlowRuleIntent.class::isInstance)
                        .findFirst()
                        .orElse(null);

        //FlowRules computed by the OpticalConnectivityIntentCompiler includes 3 criteria, one of those
        //is the OchSignal, thus retrieve used ochSignal from the selector of one of the installed rules
        if (installableIntent != null) {
            OchSignal signal = installableIntent.flowRules().stream()
                    .filter(r -> r.selector().criteria().size() == 3)
                    .map(r -> ((OchSignalCriterion)
                            r.selector().getCriterion(Criterion.Type.OCH_SIGID)).lambda())
                    .findFirst()
                    .orElse(null);

            return signal;
        } else {
            return null;
        }
    }

    private int getIntentId(Intent intent) {

        return Integer.decode(intent.id().toString());
    }
}

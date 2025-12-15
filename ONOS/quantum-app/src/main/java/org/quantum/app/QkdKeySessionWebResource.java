package org.quantum.app;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
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

import static org.onlab.util.Tools.readTreeFromStream;

/**
 * Key sessions control APIs.
 */
@Path("keySessions")
public class QkdKeySessionWebResource extends AbstractWebResource {
    private final Logger log = LoggerFactory.getLogger(getClass());

    QkdAppManager appManager = get(QkdAppManager.class);

    QkdKeySessionManager sessionManager = get(QkdKeySessionManager.class);

    QkdLinkManager linkManager = get(QkdLinkManager.class);

    RestUtilities restUtilities = get(RestUtilities.class);

    /**
     * Get list of key sessions.
     *
     * @return 200 OK
     */
    @GET
    @Path("getKeySessions")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getKeySessions() {

        ArrayNode sessions = mapper().createArrayNode();

        for (QkdKeySession session : sessionManager.getKeySessions()) {
            ObjectNode node = mapper().createObjectNode()
                    .put("master_sae_id", session.appMaster.saeId)
                    .put("slave_sae_id", session.appSlave.saeId)
                    .put("app_id", session.appId)
                    .put("link_id", session.linkId);

            sessions.add(node);
        }

        ObjectNode root = this.mapper().createObjectNode().putPOJO("QkdKeySessions", sessions);
        return ok(root).build();
    }

    /**
     * Get details on a specific key session.
     *
     * @param key app_id
     * @return 200 OK
     */
    @GET
    @Path("getKeySession/appId")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getKeySessionAppId(@QueryParam("app_id") String key) {

        ObjectNode response = mapper().createObjectNode();

        if (sessionManager.getKeySession(key) == null) {
            throw new IllegalArgumentException("Specified session does not exist");
        }

        QkdKeySession session = sessionManager.getKeySession(key);

        response.put("master_sae_id", session.appMaster.saeId)
                .put("slave_sae_id", session.appSlave.saeId)
                .put("app_id", session.appId)
                .put("link_id", session.linkId);

        return ok(response).build();
    }

    /**
     * Get details on a specific key session.
     *
     * @param appMaster the sae_id of master app
     * @param appSlave the sae_id of slave app
     * @return 200 OK
     */
    @GET
    @Path("getKeySession/apps")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getKeySession(@QueryParam("sae_master") String appMaster,
                                  @QueryParam("sae_slave") String appSlave) {

        ObjectNode response = mapper().createObjectNode();

        String prefix = appMaster.split("-")[3];
        String suffix = appSlave.split("-")[3];

        String appId = "dddddddd-" + prefix + "-dddd-" + suffix + "-dddddddddddd";

        if (sessionManager.getKeySession(appId) == null) {
            throw new IllegalArgumentException("Specified session does not exist");
        }

        QkdKeySession session = sessionManager.getKeySession(appId);

        response.put("master_sae_id", session.appMaster.saeId)
                .put("slave_sae_id", session.appSlave.saeId)
                .put("app_id", session.appId)
                .put("link_id", session.linkId);

        return ok(response).build();
    }


    /**
     * Open Key Session between two apps.
     *
     * @return 200 OK
     * @onos.rsModel appOpenKeySession
     */
    @POST
    @Path("openKeySession")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response openKeySession(InputStream stream) {
        String masterSae, slaveSae;
        QkdApp master, slave;
        QkdKeySession keySession;

        try {
            ObjectNode root = readTreeFromStream(mapper(), stream);

            master = appManager.getQkdApp(root.get("masterAddress").asText(), root.get("masterPort").asText());
            slave = appManager.getQkdApp(root.get("slaveAddress").asText(), root.get("slavePort").asText());

            if ((master == null)) {
                throw new IllegalArgumentException("Master app is not registered");
            }

            if ((slave == null)) {
                throw new IllegalArgumentException("Slave app is not registered");
            }

            if ((master == slave)) {
                throw new IllegalArgumentException("Master and slave apps must be different");
            }

            if (master.device.id().equals(slave.device.id())) {
                throw new IllegalArgumentException("Master and slave apps are co-located");
            }

            masterSae = master.saeId;
            slaveSae = slave.saeId;

            keySession = new QkdKeySession(
                    master,
                    slave
            );

            //Check if a suitable quantum link has been already created
            if (linkManager.getQkdLink(master.qkdNode, slave.qkdNode) == null) {
                throw new IllegalArgumentException("A quantum link is not available for this session");
            }

            if (linkManager.getQkdLink(master.qkdNode, slave.qkdNode).linkStatus == QkdLink.LinkStatus.OFF) {
                throw new IllegalArgumentException("The requested quantum link is OFF");
            }

            //Agent configuration via netconf
            sessionManager.configureKeySession(keySession);

            //Configure the km using REST APIs - NOT ANYMORE REQUIRED
            /*log.info("REST APIs at km may be invoked with {}", keySession.linkId);
            if (keySession.linkId.toString().equals("bbbbbbbb-7901-bbbb-7092-bbbbbbbbbbbb")) {

                log.info("REST APIs at km INVOKED slavesae {} app {} link {}",
                        slaveSae,
                        keySession.appId,
                        keySession.linkId);

                restUtilities.postKeyManager("10.79.1.47", "8001", slaveSae, keySession.appId, keySession.linkId);
                restUtilities.postKeyManager("10.79.1.47", "8002", slaveSae, keySession.appId, keySession.linkId);
            }*/

        } catch (IOException ioe) {
            throw new IllegalArgumentException(ioe);
        }

        //Add the key session to the local database
        sessionManager.addKeySession(keySession.appId, keySession);

        ObjectNode node = mapper().createObjectNode()
                .put("master_sae_id", masterSae)
                .put("slave_sae_id", slaveSae)
                //.put("link_id", keySession.linkId)
                .put("app_id", keySession.appId);

        return Response.ok(node).build();
    }

    /**
     * Close a key session.
     *
     * @param key app_id of the session
     * @return 200 OK
     */
    @DELETE
    @Path("closeKeySession")
    public Response deleteSession(@QueryParam("key") String key) {


        if (sessionManager.getKeySession(key) == null) {
            throw new IllegalArgumentException("Key session does not exist");
        }

        QkdKeySession keySession = sessionManager.getKeySession(key);

        //Delete configuration on devices
        sessionManager.deleteKeySession(keySession);

        //Delete from local database
        sessionManager.removeKeySession(key);

        return Response.ok().build();
    }
}

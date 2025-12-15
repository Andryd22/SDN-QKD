/*
 * Copyright 2023-present Open Networking Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.quantum.app;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.onlab.util.ItemNotFoundException;
import org.onosproject.rest.AbstractWebResource;

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
 * Cryptographic apps control APIs.
 */
@Path("apps")
public class QkdAppWebResource extends AbstractWebResource {
    QkdAppManager appManager = get(QkdAppManager.class);

    //HostProviderService hostProvider = get(HostProviderService.class);

    /**
     * Get list of registered apps.
     *
     * @return 200 OK
     */
    @GET
    @Path("getApps")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getQkdApps() {

        ArrayNode apps = mapper().createArrayNode();

        for (QkdApp app : appManager.getQkdApps()) {
            ObjectNode node = mapper().createObjectNode()
                    .put("app_address", app.appAddress)
                    .put("app_port", app.appPort)
                    .put("sae_id", app.saeId)
                    .put("km_location", app.device.id().toString());

            apps.add(node);
        }

        ObjectNode root = this.mapper().createObjectNode().putPOJO("QkdApps", apps);
        return ok(root).build();
    }

    /**
     * Get details on a registered app.
     *
     * @param  key sae_id of the app
     * @return 200 OK
     */
    @GET
    @Path("getApp")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getKeyManager(@QueryParam("key") String key) {
        ObjectNode response = mapper().createObjectNode();

        if (appManager.getQkdApp(key) == null) {
            throw new IllegalArgumentException("Specified app does not exist");
        }

        QkdApp app = appManager.getQkdApp(key);

        response.put("app_address", app.appAddress)
                .put("app_port", app.appPort)
                .put("sae_id", app.saeId)
                .put("km_address", app.qkdNode.kmAddress)
                .put("km_port", app.qkdNode.kmPort)
                .put("km_id", app.qkdNode.kmId);

        return ok(response).build();
    }

    /**
     * Register an app.
     *
     * @param stream input json
     * @return 200 OK
     * @onos.rsModel appRegistration
     */
    @POST
    @Path("appRegistration")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response explicitRegistration(InputStream stream) {
        QkdApp app;

        try {
            ObjectNode root = readTreeFromStream(mapper(), stream);

            app = new QkdApp(
                    root.get("appAddress").asText(),
                    root.get("appPort").asText(),
                    root.get("kmAddress").asText(),
                    root.get("kmPort").asText());

        } catch (IOException ioe) {
            throw new IllegalArgumentException(ioe);
        }

        //TODO post a host to the gui
        /*HostDescription hostDescription = new DefaultHostDescription(
                MacAddress.NONE,
                VlanId.NONE,
                new HostLocation(app.connectPoint,1234));

        hostProvider.hostDetected(HostId.hostId(app.appAddress),hostDescription,true);*/

        ObjectNode node = mapper().createObjectNode()
                .put("sae_id", app.saeId);

        return Response.ok(node).build();
    }

    /**
     * Unregister an app.
     *
     * @param key sae_id of the app
     * @return 200 OK
     */
    @DELETE
    @Path("appDelete")
    public Response deleteApp(@QueryParam("key") String key) {

        if (appManager.getQkdApp(key) == null) {
            throw new ItemNotFoundException("Application is not registered");
        }

        appManager.removeQkdApp(key);

        return Response.ok().build();
    }
}

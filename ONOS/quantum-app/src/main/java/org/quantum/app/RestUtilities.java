package org.quantum.app;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.onlab.util.Frequency;
import org.onosproject.net.*;
import org.osgi.service.component.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Base64;
import java.util.Iterator;
import java.util.Scanner;

import static org.onlab.util.Tools.nullIsIllegal;
import static org.onlab.util.Tools.readTreeFromStream;

@Component(immediate = true, service = RestUtilities.class)
public class RestUtilities {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Activate
    protected void activate() {

        log.info("REST utilities component has been STARTED");
    }

    @Deactivate
    protected void deactivate() {

        log.info("REST utilities component has been STOPPED");
    }

    protected void postKeyManager(String kmAddress, String kmPort, String slaveSae, String appId, String linkId) {
        //Build the POST json
        ObjectMapper mapper = new ObjectMapper();

        ObjectNode objectNode = mapper.createObjectNode();
        objectNode.put("link", linkId);
        //objectNode.put("link", "link1");
        //objectNode.put("app_id", appId);
        //objectNode.put("link_id", linkId);

        log.info("Configuration sent to KM {} json {}",
                kmAddress + ":" + kmPort,
                objectNode);

        try {
            //TODO complete the path
            URL url = new URL("http://" + kmAddress + ":" + kmPort + "/api/v1/keys/" + appId + "/register/");
            log.info("Sending REST request {}", url);
            try {

                //String loginPassword = "karaf:karaf";
                //String encoded = Base64.getEncoder().encodeToString(loginPassword.getBytes());

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                //conn.setRequestProperty ("Authorization", "Basic " + encoded);
                conn.setRequestProperty("accept", "application/json");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("X-CSRFToken", "ZR0Cisy3lC6TmqFlXrG31Jn8tWKTgzxZocAG468W64M6SetNhCDkHuYKS8ooEm9K");
                conn.setRequestMethod("POST");

                conn.setDoOutput(true);
                OutputStream os = conn.getOutputStream();
                os.write(objectNode.toString().getBytes());
                os.flush();
                os.close();

                InputStream responseStream = conn.getResponseCode() / 100 == 2
                        ? conn.getInputStream()
                        : conn.getErrorStream();
                Scanner s = new Scanner(responseStream).useDelimiter("\\A");
                String response = s.hasNext() ? s.next() : "";

                log.info(conn.getRequestMethod());
                log.info(String.valueOf(conn.getResponseCode()));
                log.info(conn.getResponseMessage());
                log.info(response);

                //ObjectNode root = readTreeFromStream(mapper, conn.getInputStream());
                //log.info(root.toString());

            } catch (IOException exe) {
                exe.printStackTrace();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

}

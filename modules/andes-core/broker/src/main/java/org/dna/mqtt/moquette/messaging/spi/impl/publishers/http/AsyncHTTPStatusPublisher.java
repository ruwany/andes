/*
 *  Copyright (c) 2019, Entgra (pvt) Ltd. (http://entgra.io)
 *
 *  All Rights Reserved.
 *
 *  Unauthorized copying of this file, via any medium is strictly prohibited.
 *  Proprietary and confidential.
 */

package org.dna.mqtt.moquette.messaging.spi.impl.publishers.http;

import com.google.gson.JsonObject;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import sun.nio.ch.IOUtil;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class AsyncHTTPStatusPublisher implements Runnable{

    private String endpoint;
    private String tenantDomain;
    private String deviceType;
    private String deviceId;
    private String status;
    private String username;
    private String password;

    private static Log log = LogFactory.getLog(AsyncHTTPStatusPublisher.class);

    public AsyncHTTPStatusPublisher(String endpoint, String tenantDomain, String deviceType, String deviceId, String status,
                                    String username, String password){
        this.endpoint = endpoint;
        this.tenantDomain = tenantDomain;
        this.deviceType = deviceType;
        this.deviceId = deviceId;
        this.status = status;
        this.username = username;
        this.password = password;
    }

    public void notifyDeviceStatus() {
        try {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("tenantDomain", tenantDomain);
            jsonObject.addProperty("deviceType", deviceType);
            jsonObject.addProperty("deviceId", deviceId);
            jsonObject.addProperty("status", status);

            HttpURLConnection urlConnection =
                    (HttpURLConnection) new URL(endpoint).openConnection();

            urlConnection.setRequestMethod("PUT");
            urlConnection.setRequestProperty("Accept", "application/json");
            urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);
            byte[] basicAuthHeader = (username+":"+password).getBytes("UTF-8");
            String encoded = javax.xml.bind.DatatypeConverter.printBase64Binary(basicAuthHeader);
            urlConnection.setRequestProperty("Authorization", "Basic "+encoded);

            OutputStreamWriter writer = new OutputStreamWriter(urlConnection.getOutputStream(), "UTF-8");
            writer.write(jsonObject.toString());
            writer.close();

            try (InputStreamReader response = new InputStreamReader(urlConnection.getInputStream())) {
                // convert to Object
                log.info("Published Client Connectivity Status " +
                         "Response-Code : " + urlConnection.getResponseCode() + " Message : " + IOUtils.toString(response));
            }

        } catch (IOException e ) {
            log.error("Error while Publishing Connectivity Information ", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        notifyDeviceStatus();
    }
}

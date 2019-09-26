/*
 *  Copyright (c) 2019, Entgra (pvt) Ltd. (http://entgra.io)
 *
 *  All Rights Reserved.
 *
 *  Unauthorized copying of this file, via any medium is strictly prohibited.
 *  Proprietary and confidential.
 */

package org.dna.mqtt.moquette.messaging.spi.impl.publishers.http;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.InputStreamReader;
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
            //TODO: Get endpoint from config file
            String url = endpoint  + "/" + tenantDomain + "/" +
                         deviceType + "/" + deviceId + "/" + status;
            HttpURLConnection urlConnection =
                    (HttpURLConnection) new URL(url).openConnection();

            urlConnection.setRequestMethod("PUT");
            byte[] message = (username+":"+password).getBytes("UTF-8");
            String encoded = javax.xml.bind.DatatypeConverter.printBase64Binary(message);
            urlConnection.setRequestProperty("Authorization", "Basic "+encoded);

            try (InputStreamReader response = new InputStreamReader(urlConnection.getInputStream())) {
                // convert to Object
                log.info("Published Client Connectivity Status " +
                         "Response-Code : " + urlConnection.getResponseCode() + " Message : " + response);
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

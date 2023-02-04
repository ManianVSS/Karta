package org.mvss.karta.framework.restclient;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;

import javax.net.ssl.HostnameVerifier;

@Log4j2
public class ApacheHTTPClientUtils {
    @Getter
    private static final TrustStrategy trustStrategy = (x509Certificates, s) -> true;

    @Getter
    private static final HostnameVerifier hostnameVerifier = (s, sslSession) -> true;

    @Getter
    private static SSLConnectionSocketFactory sslConnectionSocketFactory;

    static {
        try {
            sslConnectionSocketFactory = new SSLConnectionSocketFactory(new SSLContextBuilder().loadTrustMaterial(trustStrategy).build(),
                    hostnameVerifier);
        } catch (Exception e) {
            log.error("", e);
        }
    }

    public static CloseableHttpClient getInsecureHTTPSClient() {
        return HttpClients.custom().setSSLSocketFactory(sslConnectionSocketFactory).build();
    }

}

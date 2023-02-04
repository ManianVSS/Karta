package org.mvss.karta.framework.configuration;

import lombok.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.regex.Pattern;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ProxyOptions implements Serializable {
    private static final long serialVersionUID = 1L;

    private String host = "localhost";
    private int port = 80;
    private String protocol = "http";
    private boolean proxyAuthentication = false;
    private String username;
    private String password;
    private String noProxy = "localhost,127\\.*,\\[::1\\],10\\.*,192\\.168\\.*,172\\.*";

    private transient ArrayList<Pattern> noProxyPatterns = new ArrayList<>();
    private boolean initComplete = false;

    public ProxyOptions host(String host) {
        this.host = host;
        return this;
    }

    public ProxyOptions port(int port) {
        this.port = port;
        return this;
    }

    public ProxyOptions protocol(String protocol) {
        this.protocol = protocol;
        return this;
    }

    public ProxyOptions proxyAuthentication(boolean proxyAuthentication) {
        this.proxyAuthentication = proxyAuthentication;
        return this;
    }

    public ProxyOptions username(String username) {
        this.proxyAuthentication = true;
        this.username = username;
        return this;
    }

    public ProxyOptions password(String password) {
        this.proxyAuthentication = true;
        this.password = password;
        return this;
    }

    public ProxyOptions noProxy(String noProxy) {
        this.noProxy = noProxy;
        return this;
    }

    public synchronized void initPatterns() {
        noProxyPatterns.clear();
        String[] noProxyStrPatterns = noProxy.split(",");
        for (String noProxyStrPattern : noProxyStrPatterns) {
            noProxyPatterns.add(Pattern.compile(noProxyStrPattern));
        }
    }

    public synchronized ProxyOptions init() {
        if (!initComplete) {
            initPatterns();
            initComplete = true;
        }
        return this;
    }

    public boolean isNonProxyHost(String hostname) {
        for (Pattern noProxyPattern : noProxyPatterns) {
            if (noProxyPattern.matcher(hostname).matches()) {
                return true;
            }
        }
        return false;
    }
}

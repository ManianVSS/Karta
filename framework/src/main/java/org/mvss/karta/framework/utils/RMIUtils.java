package org.mvss.karta.framework.utils;

import javax.rmi.ssl.SslRMIClientSocketFactory;
import javax.rmi.ssl.SslRMIServerSocketFactory;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.util.HashMap;

/**
 * Utility class to create a connect with RMI registry
 *
 * @author Manian
 */
public class RMIUtils {
    public static final String JAVA_RMI_SERVER_HOSTNAME = "java.rmi.server.hostname";

    /**
     * Creates a new RMI registry
     */
    public static Registry createNewRegistry(String host, int port, boolean enableSSL, RMIClientSocketFactory csf, RMIServerSocketFactory ssf) throws RemoteException {
        Registry rmiRegistry;

        String prevRmiHost = System.setProperty(JAVA_RMI_SERVER_HOSTNAME, host);

        if (enableSSL) {
            rmiRegistry = LocateRegistry.createRegistry(port, csf, ssf);
        } else {
            rmiRegistry = LocateRegistry.createRegistry(port);
        }

        if (prevRmiHost == null) {
            System.clearProperty(JAVA_RMI_SERVER_HOSTNAME);
        } else {
            System.setProperty(JAVA_RMI_SERVER_HOSTNAME, prevRmiHost);
        }

        return rmiRegistry;
    }

    /**
     * Creates a new RMI registry
     */
    public static Registry createNewRegistry(String host, int port, boolean enableSSL) throws RemoteException {
        return createNewRegistry(host, port, enableSSL, new SslRMIClientSocketFactory(), new SslRMIServerSocketFactory());
    }

    /**
     * Creates a new RMI registry on any available port and returns port as well
     */
    public static HashMap<String, Object> createNewRegistryOnAnyAvailablePort(String host, boolean enableSSL) throws RemoteException {
        HashMap<String, Object> returnMap = new HashMap<>();

        Registry callBackRegistry;

        String prevRmiHost = System.setProperty(JAVA_RMI_SERVER_HOSTNAME, host);

        AvailablePortProxyRMISocketFactory socketFactory;

        if (enableSSL) {
            socketFactory = new AvailablePortProxyRMISocketFactory(new SslRMIClientSocketFactory(), new SslRMIServerSocketFactory());
        } else {
            socketFactory = new AvailablePortProxyRMISocketFactory();
        }

        callBackRegistry = createNewRegistry(host, 0, enableSSL, new SslRMIClientSocketFactory(), new SslRMIServerSocketFactory());

        if (prevRmiHost == null) {
            System.clearProperty(JAVA_RMI_SERVER_HOSTNAME);
        } else {
            System.setProperty(JAVA_RMI_SERVER_HOSTNAME, prevRmiHost);
        }

        returnMap.put("registry", callBackRegistry);
        returnMap.put("port", socketFactory.getPort());
        return returnMap;
    }

    /**
     * Tries to connect to and return a remote RMI registry
     */
    public static Registry getRemoteRegistry(String host, int port, boolean enableSSL) throws RemoteException {
        Registry rmiServerRegistry;

        if (enableSSL) {
            rmiServerRegistry = LocateRegistry.getRegistry(host, port, new SslRMIClientSocketFactory());
        } else {
            rmiServerRegistry = LocateRegistry.getRegistry(host, port);
        }
        return rmiServerRegistry;
    }
}

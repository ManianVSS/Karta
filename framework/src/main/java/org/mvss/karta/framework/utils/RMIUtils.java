package org.mvss.karta.framework.utils;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.util.HashMap;

import javax.rmi.ssl.SslRMIClientSocketFactory;
import javax.rmi.ssl.SslRMIServerSocketFactory;

public class RMIUtils
{
   public static final String JAVA_RMI_SERVER_HOSTNAME = "java.rmi.server.hostname";

   public static Registry createNewRegistry( String host, int port, boolean enableSSL, RMIClientSocketFactory csf, RMIServerSocketFactory ssf ) throws RemoteException
   {
      Registry callBackRegistry;

      String prevRmiHost = System.setProperty( JAVA_RMI_SERVER_HOSTNAME, host );

      if ( enableSSL )
      {
         callBackRegistry = LocateRegistry.createRegistry( port, csf, ssf );
      }
      else
      {
         callBackRegistry = LocateRegistry.createRegistry( port );
      }

      if ( prevRmiHost == null )
      {
         System.clearProperty( JAVA_RMI_SERVER_HOSTNAME );
      }
      else
      {
         System.setProperty( JAVA_RMI_SERVER_HOSTNAME, prevRmiHost );
      }

      return callBackRegistry;
   }

   public static Registry createNewRegistry( String host, int port, boolean enableSSL ) throws RemoteException
   {
      return createNewRegistry( host, port, enableSSL, new SslRMIClientSocketFactory(), new SslRMIServerSocketFactory() );
   }

   public static HashMap<String, Object> createNewRegistryOnAnyAvailablePort( String host, boolean enableSSL ) throws RemoteException
   {
      HashMap<String, Object> returnMap = new HashMap<String, Object>();

      Registry callBackRegistry;

      String prevRmiHost = System.setProperty( JAVA_RMI_SERVER_HOSTNAME, host );

      AvailablePortProxyRMISocketFactory socketFactory;

      if ( enableSSL )
      {
         socketFactory = new AvailablePortProxyRMISocketFactory( new SslRMIClientSocketFactory(), new SslRMIServerSocketFactory() );
      }
      else
      {
         socketFactory = new AvailablePortProxyRMISocketFactory();
      }

      callBackRegistry = createNewRegistry( host, 0, enableSSL, new SslRMIClientSocketFactory(), new SslRMIServerSocketFactory() );

      if ( prevRmiHost == null )
      {
         System.clearProperty( JAVA_RMI_SERVER_HOSTNAME );
      }
      else
      {
         System.setProperty( JAVA_RMI_SERVER_HOSTNAME, prevRmiHost );
      }

      returnMap.put( "registry", callBackRegistry );
      returnMap.put( "port", socketFactory.getPort() );
      return returnMap;
   }

   public static Registry getRemoteRegistry( String host, int port, boolean enableSSL ) throws RemoteException
   {
      Registry rmiServerRegistry = null;

      if ( enableSSL )
      {
         rmiServerRegistry = LocateRegistry.getRegistry( host, port, new SslRMIClientSocketFactory() );
      }
      else
      {
         rmiServerRegistry = LocateRegistry.getRegistry( host, port );
      }
      return rmiServerRegistry;
   }
}

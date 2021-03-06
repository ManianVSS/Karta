package org.mvss.karta.framework.utils;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * This class implements RMIClientSocketFactory and RMIServerSocketFactory to allow listening on any available port.
 * The actual port assigned is saved and the implementation is proxied to the provided implementations of RMIServerSocketFactory and RMIServerSocketFactory like SslRMIClientSocketFactory and SslRMIServerSocketFactory.
 * 
 * @author Manian
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AvailablePortProxyRMISocketFactory implements RMIClientSocketFactory, RMIServerSocketFactory
{
   private RMIClientSocketFactory clientSocketFactoryToProxy;
   private RMIServerSocketFactory serverSocketFactoryToProxy;

   /**
    * The actual port assigned for RMI server/client. Use the getter getPort() to obtain value.
    */
   private int                    port;

   public AvailablePortProxyRMISocketFactory( RMIClientSocketFactory clientSocketFactoryToProxy, RMIServerSocketFactory serverSocketFactoryToProxy )
   {
      this.clientSocketFactoryToProxy = clientSocketFactoryToProxy;
      this.serverSocketFactoryToProxy = serverSocketFactoryToProxy;
   }

   @Override
   public Socket createSocket( String host, int port ) throws IOException
   {
      if ( port == 0 )
      {
         if ( this.port != 0 )
         {
            port = this.port;
         }
         else
         {
            throw new IOException( "Create a server socket before creating a client socket if using port as 0" );
         }
      }

      return clientSocketFactoryToProxy.createSocket( host, port );
   }

   @Override
   public ServerSocket createServerSocket( int port ) throws IOException
   {
      ServerSocket socket = serverSocketFactoryToProxy.createServerSocket( port );

      if ( port == 0 )
      {
         this.port = socket.getLocalPort();
      }
      else
      {
         this.port = port;
      }

      return socket;
   }
}
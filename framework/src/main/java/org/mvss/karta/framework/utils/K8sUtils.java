package org.mvss.karta.framework.utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.mvss.karta.framework.runtime.Constants;
import org.mvss.karta.framework.runtime.interfaces.PropertyMapping;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.BatchV1Api;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodBuilder;
import io.kubernetes.client.util.Config;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Getter
public class K8sUtils
{
   private static final String KUBERNETES_SERVICE_HOST      = "KUBERNETES_SERVICE_HOST";
   private static final String KUBERNETES_PORT_443_TCP_PORT = "KUBERNETES_PORT_443_TCP_PORT";

   private static final String TOKEN_PATH                   = "/var/run/secrets/kubernetes.io/serviceaccount/token";

   private CoreV1Api           coreV1Api;
   private BatchV1Api          batchV1Api;

   @PropertyMapping( group = "K8sUtils", value = "token" )
   private String              token;

   private K8sUtils()
   {
      init();
   }

   public void init()
   {
      if ( StringUtils.isAllEmpty( token ) )
      {
         Path tokenPath = Paths.get( TOKEN_PATH );

         try
         {
            if ( Files.exists( tokenPath ) )
            {
               token = FileUtils.readFileToString( tokenPath.toFile(), StandardCharsets.UTF_8 );
            }
            else
            {
               log.error( "K8s token file not found: " + TOKEN_PATH );
               return;
            }
         }
         catch ( IOException e )
         {
            log.error( "Error while reading token from token file", e );
         }

         if ( StringUtils.isAllEmpty( token ) )
         {
            log.error( "K8s token could not be fetched..." );
            return;
         }
      }

      String basePath = Constants.HTTPS + System.getenv( KUBERNETES_SERVICE_HOST ) + Constants.COLON + System.getenv( KUBERNETES_PORT_443_TCP_PORT );
      ApiClient apiClient = Config.fromToken( basePath, token, false );
      Configuration.setDefaultApiClient( apiClient );
      coreV1Api = new CoreV1Api();
      batchV1Api = new BatchV1Api();
   }

   public V1Pod createPod( String namespace, String name, String image, Map<String, String> labels, boolean tty ) throws IOException, ApiException
   {
      V1Pod pod = new V1PodBuilder().withNewMetadata().withName( name ).withLabels( labels ).endMetadata().withNewSpec().addNewContainer().withName( name ).withImage( image ).withTty( tty ).endContainer().endSpec().build();
      return coreV1Api.createNamespacedPod( namespace, pod, null, null, null );
   }
}

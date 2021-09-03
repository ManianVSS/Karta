package org.mvss.karta.framework.utils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import java.util.Random;

public class FileUtils
{
   private static Random random = new Random();

   public static byte[] getAllBytesInResourceFile( String resourceName ) throws IOException
   {
      return readContentFromInputStream( Objects.requireNonNull( FileUtils.class.getResourceAsStream( "/" + resourceName ) ) );
   }

   public static String getAllTextInResourceFile( String resourceName ) throws IOException
   {
      return readStringFromInputStream( FileUtils.class.getResourceAsStream( "/" + resourceName ) );
   }

   public static byte[] readContentFromInputStream( InputStream inputStream ) throws IOException
   {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();

      byte[] buffer = new byte[65536];

      int bytesRead = 0;

      while ( ( bytesRead = inputStream.read( buffer, 0, buffer.length ) ) > 0 )
      {
         baos.write( buffer, 0, bytesRead );
      }

      return baos.toByteArray();
   }

   public static void writeStreamToFile( InputStream inputStream, String fileName ) throws IOException
   {
      try (FileOutputStream fout = new FileOutputStream( new File( fileName ) ))
      {
         byte[] buffer = new byte[65536];

         int bytesRead = 0;

         while ( ( bytesRead = inputStream.read( buffer, 0, buffer.length ) ) > 0 )
         {
            fout.write( buffer, 0, bytesRead );
         }
      }
   }

   public static void createFileFromString( String fileName, String fileContent ) throws Exception
   {

      File file = new File( fileName );
      if ( file.exists() )
      {
         file.delete();
         file.createNewFile();
      }
      else
      {
         file.createNewFile();
      }
      FileWriter writer = new FileWriter( file );
      writer.write( fileContent );
      writer.close();
   }

   public static String readStringFromInputStream( InputStream inputStream ) throws IOException
   {
      StringBuilder resultStringBuilder = new StringBuilder();
      try (BufferedReader br = new BufferedReader( new InputStreamReader( inputStream ) ))
      {
         String line;
         while ( ( line = br.readLine() ) != null )
         {
            resultStringBuilder.append( line ).append( "\n" );
         }
      }
      return resultStringBuilder.toString();
   }

   public static byte[] getAllBytesInFile( String fileName, boolean isResource ) throws IOException
   {
      return isResource ? getAllBytesInResourceFile( fileName ) : Files.readAllBytes( Paths.get( fileName ) );
   }

   public static String getAllTextInFile( String fileName, boolean isResource ) throws IOException
   {
      return isResource ? getAllTextInResourceFile( fileName ) : new String( Files.readAllBytes( Paths.get( fileName ) ) );
   }

   public static void writeAllTextToFile( String text, String fileName ) throws IOException
   {
      writeAllTextToFile( text, Paths.get( fileName ) );
   }

   public static void writeAllTextToFile( String text, Path path ) throws IOException
   {
      Files.write( path, text.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING );
   }

   public static void writeAllBytesToFile( byte[] contents, String fileName ) throws IOException
   {
      writeAllBytesToFile( contents, Paths.get( fileName ) );
   }

   public static void writeAllBytesToFile( byte[] contents, Path path ) throws IOException
   {
      Files.write( path, contents, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING );
   }

   public static void createRandomFile( String fileName, long fileSize, int BLOCK_SIZE ) throws IOException
   {
      try (OutputStream os = Files.newOutputStream( Paths.get( fileName ) ))
      {
         long bytesToWrite = fileSize;

         byte[] writeBuffer = new byte[BLOCK_SIZE];

         while ( bytesToWrite >= BLOCK_SIZE )
         {
            random.nextBytes( writeBuffer );
            os.write( writeBuffer );
            bytesToWrite -= BLOCK_SIZE;
         }
         if ( bytesToWrite > 0 )
         {
            writeBuffer = new byte[(int) bytesToWrite];
            random.nextBytes( writeBuffer );
            os.write( writeBuffer );
         }
      }
   }
}
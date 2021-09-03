package org.mvss.karta.framework.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Getter
public class SSHUtil implements AutoCloseable
{
   protected JSch    jsch    = new JSch();
   protected Session session = null;

   protected String  user;
   protected String  pass;
   protected String  host;
   protected int     port;

   public void connect() throws JSchException
   {
      if ( ( session == null ) || !session.isConnected() )
      {
         session = jsch.getSession( user, host, port );
         UserInfo ui = new ByPassUserInfo( pass );
         session.setUserInfo( ui );

         Properties config = new Properties();
         config.setProperty( "StrictHostKeyChecking", "no" );
         session.setConfig( config );

         session.connect();
      }
   }

   public SSHUtil()
   {

   }

   /**
    * @param user
    * @param pass
    * @param host
    * @throws JSchException
    */
   public SSHUtil( String user, String pass, String host ) throws JSchException
   {
      this( user, pass, host, 22 );
   }

   /**
    * @param user
    * @param pass
    * @param host
    * @param port
    * @throws JSchException
    */
   public SSHUtil( String user, String pass, String host, int port ) throws JSchException
   {
      init( user, pass, host, port );
   }

   protected void init( String user, String pass, String host, int port ) throws JSchException
   {
      this.user = user;
      this.pass = pass;
      this.host = host;
      this.port = port;

      connect();
   }

   /**
    * 
    */
   @Override
   public void close()
   {
      if ( ( session != null ) && ( session.isConnected() ) )
      {
         session.disconnect();
         session = null;
      }
   }

   /*
    * (non-Javadoc)
    * @see java.lang.Object#finalize()
    */
   @Override
   protected void finalize() throws Throwable
   {
      close();
   }

   public int executeCommand( String command ) throws Exception
   {
      return executeCommand( command, System.out );
   }

   public int executeCommand( String command, boolean sudo ) throws Exception
   {
      connect();
      return sudo ? executeSudoCommand( command, System.out ) : executeCommand( command, System.out );
   }

   public int executeCommand( String command, PrintStream outputStream ) throws Exception
   {
      int exitCode = -1;
      Channel channel = session.openChannel( "exec" );
      ( (ChannelExec) channel ).setCommand( command );
      channel.setInputStream( null );
      InputStream in = channel.getInputStream();
      ( (ChannelExec) channel ).setErrStream( outputStream != null ? outputStream : System.err, true );
      channel.connect();
      byte[] tmp = new byte[1024];
      while ( true )
      {
         while ( in.available() > 0 )
         {
            int i = in.read( tmp, 0, 1024 );
            if ( i < 0 )
            {
               break;
            }
            if ( outputStream != null )
            {
               outputStream.print( new String( tmp, 0, i ) );
            }
         }
         if ( channel.isClosed() )
         {
            if ( in.available() > 0 )
            {
               continue;
            }
            exitCode = channel.getExitStatus();
            break;
         }
         try
         {
            Thread.sleep( 1000 );
         }
         catch ( Exception ee )
         {
         }
      }
      channel.disconnect();
      return exitCode;
   }

   public int executeSudoCommand( String command ) throws Exception
   {
      return executeSudoCommand( command, System.out );
   }

   public String executeCommandReturningOutput( String command, boolean useSudo ) throws Exception
   {
      connect();
      String output = null;
      int exitCode = -1;

      log.debug( "Running command " + command + " sudo=" + useSudo );
      try (ByteArrayOutputStream baos = new ByteArrayOutputStream())
      {
         if ( useSudo )
         {
            exitCode = executeSudoCommand( command, new PrintStream( baos ) );
         }
         else
         {
            exitCode = executeCommand( command, new PrintStream( baos ) );
         }
         output = baos.toString();

         if ( exitCode != 0 )
         {

         }
      }

      return output;
   }

   public Future<String> executeCommandReturningOutputFuture( String command, boolean useSudo, ExecutorService executor ) throws Exception
   {
      return executor.submit( () -> executeCommandReturningOutput( command, useSudo ) );
   }

   public String executeCommandWithTimeout( String command, boolean useSudo, long timeOut, long checkInterval ) throws Throwable
   {
      ExecutorService executor = Executors.newSingleThreadExecutor();
      Future<String> futureOutput = executeCommandReturningOutputFuture( command, useSudo, executor );
      String output = null;

      log.info( "Going to wait for the command to return output..." );
      WaitUtil.waitUntil( () -> futureOutput.isDone(), timeOut, checkInterval, WaitUtil.defaultWaitIterationTask );
      executor.shutdown();

      if ( futureOutput.isDone() )
         output = futureOutput.get();

      return output;
   }

   public int executeSudoCommand( String command, PrintStream outputStream ) throws Exception
   {
      int exitCode = -1;
      Channel channel = session.openChannel( "exec" );

      ( (ChannelExec) channel ).setCommand( "sudo -S -p '' " + command );

      InputStream in = channel.getInputStream();
      OutputStream out = channel.getOutputStream();
      ( (ChannelExec) channel ).setErrStream( outputStream != null ? outputStream : System.err, true );

      channel.connect();

      out.write( ( pass + "\n" ).getBytes() );
      out.flush();

      byte[] tmp = new byte[1024];
      while ( true )
      {
         while ( in.available() > 0 )
         {
            int i = in.read( tmp, 0, 1024 );
            if ( i < 0 )
            {
               break;
            }
            if ( outputStream != null )
            {
               outputStream.print( new String( tmp, 0, i ) );
            }
         }
         if ( channel.isClosed() )
         {
            exitCode = channel.getExitStatus();
            break;
         }
         try
         {
            Thread.sleep( 1000 );
         }
         catch ( Exception ee )
         {
         }
      }
      channel.disconnect();
      return exitCode;
   }

   public int executeSudoCommandWithoutOutput( String command ) throws Exception
   {
      Channel channel = session.openChannel( "exec" );

      ( (ChannelExec) channel ).setCommand( "sudo -S -p '' " + command );

      // InputStream in = channel.getInputStream();
      OutputStream out = channel.getOutputStream();
      ( (ChannelExec) channel ).setErrStream( System.err, true );

      channel.connect();

      out.write( ( pass + "\n" ).getBytes() );
      out.flush();
      int exitCode = channel.getExitStatus();
      channel.disconnect();
      return exitCode;
   }

   // public int executeShellCommand( String shellPath, String shellCommandFile ) throws Exception
   // {
   // return executeShellCommand( shellPath, shellCommandFile, System.out );
   // }

   // public int executeShellCommand( String shellPath, String shellCommandFile, PrintStream outputStream ) throws Exception
   // {
   // int exitCode = -1;
   // String shellCommandBaseFileName = new File( shellCommandFile ).getName();
   // String folderName = ".sshutil/temp" + System.currentTimeMillis();
   // String remoteFile = folderName + "/" + shellCommandBaseFileName;
   // executeCommand( "mkdir -p " + folderName, outputStream );
   // uploadFile( shellCommandFile, remoteFile );
   // executeCommand( "chmod u+x " + remoteFile, outputStream );
   // exitCode = executeCommand( shellPath + " " + remoteFile, outputStream );
   // executeCommand( "rm -rf " + folderName, outputStream );
   //
   // return exitCode;
   // }

   public boolean getFile( String remoteFile, String localFile, boolean useSudo ) throws Exception
   {
      try
      {
         if ( useSudo )
         {
            String tempFile = executeCommandReturningOutput( "mktemp", false ).trim();

            if ( executeSudoCommand( "cp --no-preserve=mode,ownership " + remoteFile + " " + tempFile ) != 0 )
            {
               return false;
            }

            remoteFile = tempFile;
         }
         getFile( remoteFile, localFile );
         return true;
      }
      catch ( Exception e )
      {
         log.error( "Exception occured", e );
         return false;
      }
   }

   /**
    * Method to get a remote file from SSH server
    * 
    * @param remoteFile
    * @param localFile
    * @throws Exception
    */
   public void getFile( String remoteFile, String localFile ) throws Exception
   {
      FileOutputStream fos = null;
      try
      {
         String prefix = null;
         if ( new File( localFile ).isDirectory() )
         {
            prefix = localFile + File.separator;
         }

         // exec 'scp -f rfile' remotely
         remoteFile = remoteFile.replace( "'", "'\"'\"'" );
         remoteFile = "'" + remoteFile + "'";
         String command = "scp -f " + remoteFile;
         Channel channel = session.openChannel( "exec" );
         ( (ChannelExec) channel ).setCommand( command );

         // get I/O streams for remote scp
         OutputStream out = channel.getOutputStream();
         InputStream in = channel.getInputStream();

         channel.connect();

         byte[] buf = new byte[1024];

         // send '\0'
         buf[0] = 0;
         out.write( buf, 0, 1 );
         out.flush();

         while ( true )
         {
            int c = checkAck( in );
            if ( c != 'C' )
            {
               break;
            }

            // read '0644 '
            in.read( buf, 0, 5 );

            long filesize = 0L;
            while ( true )
            {
               if ( in.read( buf, 0, 1 ) < 0 )
               {
                  // error
                  break;
               }
               if ( buf[0] == ' ' )
                  break;
               filesize = filesize * 10L + buf[0] - '0';
            }

            String file = null;
            for ( int i = 0;; i++ )
            {
               in.read( buf, i, 1 );
               if ( buf[i] == (byte) 0x0a )
               {
                  file = new String( buf, 0, i );
                  break;
               }
            }

            buf[0] = 0;
            out.write( buf, 0, 1 );
            out.flush();

            // read a content of lfile
            fos = new FileOutputStream( prefix == null ? localFile : prefix + file );
            int foo;
            while ( true )
            {
               if ( buf.length < filesize )
                  foo = buf.length;
               else
                  foo = (int) filesize;
               foo = in.read( buf, 0, foo );
               if ( foo < 0 )
               {
                  // error
                  break;
               }
               fos.write( buf, 0, foo );
               filesize -= foo;
               if ( filesize == 0L )
                  break;
            }
            fos.close();
            fos = null;

            if ( checkAck( in ) != 0 )
            {
               throw new Exception( "Acknowledgement check failed" );
            }

            // send '\0'
            buf[0] = 0;
            out.write( buf, 0, 1 );
            out.flush();
         }

         channel.disconnect();

      }
      catch ( Exception e )
      {
         log.info( e );
         try
         {
            if ( fos != null )
               fos.close();
         }
         catch ( Exception ee )
         {
            throw ee;
         }

         throw e;
      }
   }

   public void uploadFolder( String localFolder, String remoteFolder ) throws Exception
   {
      executeCommand( "mkdir -p " + remoteFolder );

      File folder = new File( localFolder );
      File[] listOfFiles = folder.listFiles();

      for ( int i = 0; i < listOfFiles.length; i++ )
      {
         String baseName = listOfFiles[i].getName();
         String absolutePath = listOfFiles[i].getAbsolutePath();

         if ( listOfFiles[i].isFile() )
         {
            uploadFile( absolutePath, remoteFolder + "/" + baseName );
         }
         else if ( listOfFiles[i].isDirectory() )
         {
            uploadFolder( absolutePath, remoteFolder + "/" + baseName );
         }
      }
   }

   public int runFile( String localFileName, String remoteFileName, String args, boolean sudo ) throws Exception
   {
      connect();
      uploadFile( localFileName, remoteFileName );
      String command = "bash " + remoteFileName + " " + args;
      int exitCode = executeCommand( command, sudo );
      return exitCode;
   }

   /**
    * Method to upload a local file to SSH server
    * 
    * @param localFile
    * @param remoteFile
    * @throws Exception
    */
   public void uploadFile( String localFile, String remoteFile ) throws Exception
   {
      connect();
      FileInputStream fis = null;

      boolean ptimestamp = false;
      String remoteBaseFileName = remoteFile;

      if ( remoteFile.lastIndexOf( '/' ) > 0 )
      {
         remoteBaseFileName = remoteFile.substring( remoteFile.lastIndexOf( '/' ) + 1 );
      }

      // exec 'scp -t rfile' remotely
      remoteFile = remoteFile.replace( "'", "'\"'\"'" );
      remoteFile = "'" + remoteFile + "'";
      String command = "scp " + ( ptimestamp ? "-p" : "" ) + " -t " + remoteFile;
      Channel channel = session.openChannel( "exec" );
      ( (ChannelExec) channel ).setCommand( command );

      // get I/O streams for remote scp
      OutputStream out = channel.getOutputStream();
      InputStream in = channel.getInputStream();

      channel.connect();

      if ( checkAck( in ) != 0 )
      {
         throw new Exception( "Acknowledgement check failed" );
      }

      File _lfile = new File( localFile );

      if ( ptimestamp )
      {
         command = "T " + ( _lfile.lastModified() / 1000 ) + " 0";
         // The access time should be sent here,
         // but it is not accessible with JavaAPI ;-<
         command += ( " " + ( _lfile.lastModified() / 1000 ) + " 0\n" );
         out.write( command.getBytes() );
         out.flush();
         if ( checkAck( in ) != 0 )
         {
            throw new Exception( "Acknowledgement check failed" );
         }
      }

      // send "C0644 filesize filename", where filename should not include '/'
      long filesize = _lfile.length();
      command = "C0644 " + filesize + " ";
      command += remoteBaseFileName;
      command += "\n";
      out.write( command.getBytes() );
      out.flush();
      if ( checkAck( in ) != 0 )
      {
         throw new Exception( "Acknowledgement check failed" );
      }

      // send a content of lfile
      fis = new FileInputStream( localFile );
      byte[] buf = new byte[1024];
      while ( true )
      {
         int len = fis.read( buf, 0, buf.length );
         if ( len <= 0 )
            break;
         out.write( buf, 0, len ); // out.flush();
      }
      fis.close();
      fis = null;
      // send '\0'
      buf[0] = 0;
      out.write( buf, 0, 1 );
      out.flush();
      if ( checkAck( in ) != 0 )
      {
         throw new Exception( "Acknowledgement check failed" );
      }
      out.close();
      channel.disconnect();
      // }
      // catch ( Exception e )
      // {
      // log.info( e );
      // try
      // {
      // if ( fis != null )
      // fis.close();
      // }
      // catch ( Exception ee )
      // {
      // }
      // }
   }

   public boolean uploadFile( String localFileName, String remoteFileName, boolean useSudo ) throws Exception
   {
      if ( useSudo )
      {
         String tempFile = executeCommandReturningOutput( "mktemp", false ).trim();
         uploadFile( localFileName, tempFile );
         executeCommand( "sudo mv " + tempFile + " " + remoteFileName, true );
      }
      else
      {
         uploadFile( localFileName, remoteFileName );
      }

      return true;
   }

   public void writeFile( byte[] content, String remoteFile ) throws Exception
   {

      InputStream fis = null;
      try
      {
         boolean ptimestamp = false;
         String remoteBaseFileName = remoteFile;

         if ( remoteFile.lastIndexOf( '/' ) > 0 )
         {
            remoteBaseFileName = remoteFile.substring( remoteFile.lastIndexOf( '/' ) + 1 );
         }

         // exec 'scp -t rfile' remotely
         remoteFile = remoteFile.replace( "'", "'\"'\"'" );
         remoteFile = "'" + remoteFile + "'";
         String command = "scp " + ( ptimestamp ? "-p" : "" ) + " -t " + remoteFile;
         Channel channel = session.openChannel( "exec" );
         ( (ChannelExec) channel ).setCommand( command );

         // get I/O streams for remote scp
         OutputStream out = channel.getOutputStream();
         InputStream in = channel.getInputStream();

         channel.connect();

         if ( checkAck( in ) != 0 )
         {
            throw new Exception( "Acknowledgement check failed" );
         }

         long modifiedTime = System.currentTimeMillis();

         if ( ptimestamp )
         {
            command = "T " + ( modifiedTime / 1000 ) + " 0";
            // The access time should be sent here,
            // but it is not accessible with JavaAPI ;-<
            command += ( " " + ( modifiedTime / 1000 ) + " 0\n" );
            out.write( command.getBytes() );
            out.flush();
            if ( checkAck( in ) != 0 )
            {
               throw new Exception( "Acknowledgement check failed" );
            }
         }

         // send "C0644 filesize filename", where filename should not include '/'
         long filesize = content.length;
         command = "C0644 " + filesize + " ";
         command += remoteBaseFileName;
         command += "\n";
         out.write( command.getBytes() );
         out.flush();
         if ( checkAck( in ) != 0 )
         {
            throw new Exception( "Acknowledgement check failed" );
         }

         // send a content of lfile
         fis = new ByteArrayInputStream( content );
         byte[] buf = new byte[1024];
         while ( true )
         {
            int len = fis.read( buf, 0, buf.length );
            if ( len <= 0 )
               break;
            out.write( buf, 0, len ); // out.flush();
         }
         fis.close();
         fis = null;
         // send '\0'
         buf[0] = 0;
         out.write( buf, 0, 1 );
         out.flush();
         if ( checkAck( in ) != 0 )
         {
            throw new Exception( "Acknowledgement check failed" );
         }
         out.close();
         channel.disconnect();
      }
      catch ( Exception e )
      {
         log.info( e );
         try
         {
            if ( fis != null )
               fis.close();
         }
         catch ( Exception ee )
         {
         }
      }
   }

   /**
    * Method to upload a local file to SSH server
    * 
    * @param remoteFile
    * @throws Exception
    */
   public void uploadStreamAsFile( InputStream is, long filesize, String remoteFile ) throws Exception
   {
      try
      {
         boolean ptimestamp = false;

         String remoteBaseFileName = remoteFile;

         if ( remoteFile.lastIndexOf( '/' ) > 0 )
         {
            remoteBaseFileName = remoteFile.substring( remoteFile.lastIndexOf( '/' ) + 1 );
         }

         // exec 'scp -t rfile' remotely
         remoteFile = remoteFile.replace( "'", "'\"'\"'" );
         remoteFile = "'" + remoteFile + "'";
         String command = "scp " + ( ptimestamp ? "-p" : "" ) + " -t " + remoteFile;
         Channel channel = session.openChannel( "exec" );
         ( (ChannelExec) channel ).setCommand( command );

         // get I/O streams for remote scp
         OutputStream out = channel.getOutputStream();
         InputStream in = channel.getInputStream();

         channel.connect();

         if ( checkAck( in ) != 0 )
         {
            throw new Exception( "Acknowledgement check failed" );
         }

         if ( ptimestamp )
         {
            long currentMillis = new Date().getTime();

            command = "T " + ( currentMillis / 1000 ) + " 0";
            // The access time should be sent here,
            // but it is not accessible with JavaAPI ;-<
            command += ( " " + ( currentMillis / 1000 ) + " 0\n" );
            out.write( command.getBytes() );
            out.flush();
            if ( checkAck( in ) != 0 )
            {
               throw new Exception( "Acknowledgement check failed" );
            }
         }

         // send "C0644 filesize filename", where filename should not include '/'
         command = "C0644 " + filesize + " " + remoteBaseFileName;

         command += "\n";
         out.write( command.getBytes() );
         out.flush();
         if ( checkAck( in ) != 0 )
         {
            throw new Exception( "Acknowledgement check failed" );
         }

         byte[] buf = new byte[1024];
         while ( true )
         {
            int len = is.read( buf, 0, buf.length );
            if ( len <= 0 )
               break;
            out.write( buf, 0, len ); // out.flush();
         }
         is.close();
         is = null;
         // send '\0'
         buf[0] = 0;
         out.write( buf, 0, 1 );
         out.flush();
         if ( checkAck( in ) != 0 )
         {
            throw new Exception( "Acknowledgement check failed" );
         }
         out.close();
         channel.disconnect();
      }
      catch ( Exception e )
      {
         log.info( e );
         try
         {
            if ( is != null )
               is.close();
         }
         catch ( Exception ee )
         {
         }
      }
   }

   /**
    * Internal method for checking acknowledgement
    * 
    * @param in
    * @return
    * @throws Exception
    */
   private static int checkAck( InputStream in ) throws Exception
   {
      int b = in.read();

      if ( b == 1 || b == 2 )
      {
         StringBuffer sb = new StringBuffer();
         int c;
         do
         {
            c = in.read();
            sb.append( (char) c );
         }
         while ( c != '\n' );

         throw new Exception( "Scp error: " + sb.toString() );
      }

      return b;
   }

   /**
    * This class is used internally to pass user password to JSch library
    * 
    * @author 212735819
    */
   private static class ByPassUserInfo implements UserInfo
   {
      private String password;

      public ByPassUserInfo( String password )
      {
         super();
         this.password = password;
      }

      @Override
      public String getPassphrase()
      {
         return null;
      }

      @Override
      public String getPassword()
      {
         return password;
      }

      @Override
      public boolean promptPassword( String message )
      {
         return true;
      }

      @Override
      public boolean promptPassphrase( String message )
      {
         return true;
      }

      @Override
      public boolean promptYesNo( String message )
      {
         return true;
      }

      @Override
      public void showMessage( String message )
      {
      }
   }
}
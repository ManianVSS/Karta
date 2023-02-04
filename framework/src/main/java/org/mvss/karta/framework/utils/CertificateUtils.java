package org.mvss.karta.framework.utils;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.mvss.karta.Constants;

import javax.net.ssl.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

@Log4j2
public class CertificateUtils {
    public static final String CHANGE_IT = "changeit";
    public static final String JAVA_HOME = "JAVA_HOME";
    public static final String JAVA_HOME1 = "java.home";
    public static final String CACERTS = "cacerts";
    public static final String EXCEPTION_OCCURRED = "Exception occurred";
    private static final char[] HEX_DIGITS = "0123456789abcdef".toCharArray();

    public static void main(String[] args) {
        if (args.length < 2) {
            log.info("Usage: java InstallCert <host>[:port] alias [passphrase] [-DJAVA_HOME=<Java_to_edit>]");
            System.exit(-1);
        }

        String[] args1 = args[0].split(Constants.COLON);
        String hostname = args1[0];
        int port = args1.length != 1 ? Integer.parseInt(args1[1]) : 443;
        String s2 = (args.length > 2) ? args[2] : CHANGE_IT;
        char[] passphrase = s2.toCharArray();
        String alias = args[1];

        try {
            if (installCertificate(null, hostname, port, passphrase, alias)) {
                log.info("Certificate installed successful");
            }
        } catch (Exception e) {
            System.exit(-1);
        }
    }

    public static X509Certificate getCertificateForServer(String hostname, int port, KeyStore keystore) {
        try {
            SSLContext sslcontext = SSLContext.getInstance("TLS");
            TrustManagerFactory trustmanagerfactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustmanagerfactory.init(keystore);
            X509TrustManager x509trustmanager = (X509TrustManager) trustmanagerfactory.getTrustManagers()[0];
            SavingTrustManager savingtrustmanager = new SavingTrustManager(x509trustmanager);
            sslcontext.init(null, new TrustManager[]{savingtrustmanager}, null);
            SSLSocketFactory sslsocketfactory = sslcontext.getSocketFactory();
            log.info("Opening connection to " + hostname + ":" + port + "...");
            SSLSocket sslsocket = (SSLSocket) sslsocketfactory.createSocket(hostname, port);
            sslsocket.setSoTimeout(10000);
            try {
                log.info("Starting SSL handshake...");
                sslsocket.startHandshake();
                sslsocket.close();
                log.info("No errors, certificate is already trusted");
            } catch (SSLException sslexception) {
                log.error("SSL Exception", sslexception);
            }
            X509Certificate[] ax509certificate = savingtrustmanager.chain;
            if (ax509certificate == null) {
                log.error("Could not obtain server certificate chain");
                return null;
            }

            log.info("Server sent " + ax509certificate.length + " certificate(s):");

            MessageDigest messageDigest = MessageDigest.getInstance("SHA1");
            MessageDigest messageDigest1 = MessageDigest.getInstance("MD5");
            for (int j = 0; j < ax509certificate.length; j++) {
                X509Certificate x509certificate = ax509certificate[j];
                log.info(" " + (j + 1) + " Subject " + x509certificate.getSubjectX500Principal());
                log.info("   Issuer  " + x509certificate.getIssuerX500Principal());
                messageDigest.update(x509certificate.getEncoded());
                log.info("   sha1    " + toHexString(messageDigest.digest()));
                messageDigest1.update(x509certificate.getEncoded());
                log.info("   md5     " + toHexString(messageDigest1.digest()));
            }

            return ax509certificate[0];
        } catch (Exception e) {
            log.error(EXCEPTION_OCCURRED, e);
            return null;
        }
    }

    public static boolean installCertificate(String jreHome, String hostname, int port, char[] passphrase, String alias) {
        if (StringUtils.isBlank(jreHome)) {
            jreHome = PropertyUtils.getSystemOrEnvProperty(JAVA_HOME, System.getProperty(JAVA_HOME1));
        }

        File jreSecurityDirectory = new File(jreHome + File.separator + "lib" + File.separator + "security");

        if (!jreSecurityDirectory.exists()) {
            log.error("jre_home specified" + jreHome + " not found or does not have security folder");
            return false;
        }

        try {
            File jreCACertsFile = new File(jreSecurityDirectory, CACERTS);
            log.info("Loading KeyStore " + jreCACertsFile + " ...");
            KeyStore keystore;
            try (FileInputStream fileinputstream = new FileInputStream(jreCACertsFile)) {
                keystore = KeyStore.getInstance(KeyStore.getDefaultType());
                keystore.load(fileinputstream, passphrase);
            }

            X509Certificate serverCertificate = getCertificateForServer(hostname, port, keystore);
            // String s3 = ( new StringBuilder() ).append( hostname ).append( "-" ).append( k + 1 ).toString();
            if (StringUtils.isBlank(alias)) {
                alias = hostname + "_" + port;
            }
            keystore.setCertificateEntry(alias, serverCertificate);
            FileOutputStream fileoutputstream = new FileOutputStream(jreCACertsFile);
            keystore.store(fileoutputstream, passphrase);
            fileoutputstream.close();

            log.info(serverCertificate);

            log.info("Added certificate to keystore 'jssecacerts' using alias '" + alias + "'");
        } catch (Exception e) {
            e.printStackTrace();
            log.error(EXCEPTION_OCCURRED, e);
            return false;
        }
        return true;
    }

    private static String toHexString(byte[] aByte) {
        StringBuilder stringbuilder = new StringBuilder(aByte.length * 3);
        for (int b : aByte) {
            int k = b;
            k &= 0xff;
            stringbuilder.append(HEX_DIGITS[k >> 4]);
            stringbuilder.append(HEX_DIGITS[k & 0xf]);
            stringbuilder.append(' ');
        }

        return stringbuilder.toString();
    }

    private static class SavingTrustManager implements X509TrustManager {
        private final X509TrustManager tm;
        private X509Certificate[] chain;

        SavingTrustManager(X509TrustManager x509trustmanager) {
            tm = x509trustmanager;
        }

        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }

        public void checkClientTrusted(X509Certificate[] ax509certificate, String s) {
            throw new UnsupportedOperationException();
        }

        public void checkServerTrusted(X509Certificate[] ax509certificate, String s) throws CertificateException {
            chain = ax509certificate;
            tm.checkServerTrusted(ax509certificate, s);
        }
    }

}

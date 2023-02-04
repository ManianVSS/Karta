package org.mvss.karta.framework.utils;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;

/**
 * Utility class for loading resources from class-path or file-system.
 *
 * @author Manian
 */
public class ClassPathLoaderUtils {
    /**
     * Get the URI of a file from file system(higher priority) or class-path resource.
     * Returns null if not found
     */
    public static URI getFileOrResourceURI(String fileName) throws URISyntaxException {
        File fileToLoad = new File(fileName);

        if (fileToLoad.exists()) {
            return fileToLoad.toURI();
        }

        URL url = ClassPathLoaderUtils.class.getResource("/" + fileName);

        return (url == null) ? null : url.toURI();
    }

    /**
     * Get the file input stream of a file from file system(higher priority) or class-path resource
     * Returns null if not found
     */
    public static InputStream getFileStream(String fileName) throws IOException {
        File fileToLoad = new File(fileName);

        if (fileToLoad.exists() && fileToLoad.isFile()) {
            return new FileInputStream(fileToLoad);
        }

        return ClassPathLoaderUtils.class.getResourceAsStream("/" + fileName);
    }

    /**
     * Get all text of a file from file system(higher priority) or class-path resource
     * Returns null if not found
     */
    public static String readAllText(String fileName) throws IOException, URISyntaxException {
        InputStream fileInputStream = getFileStream(fileName);

        if (fileInputStream == null) {
            return null;
        }
        return IOUtils.toString(fileInputStream, Charset.defaultCharset());
    }

}
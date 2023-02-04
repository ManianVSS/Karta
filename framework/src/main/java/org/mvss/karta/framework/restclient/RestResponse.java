package org.mvss.karta.framework.restclient;

import com.fasterxml.jackson.core.type.TypeReference;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;

@SuppressWarnings("unused")
public interface RestResponse extends Serializable, AutoCloseable {
    String getProtocolVersion();

    int getStatusCode();

    String getReasonPhrase();

    HashMap<String, String> getHeaders();

    ContentType getContentType();

    String getBody() throws IOException;

    <T> T getBodyAs(Class<T> type) throws IOException;

    <T> T getBodyAs(TypeReference<T> type) throws IOException;

    InputStream getStream() throws IOException;

    void downloadFile(File file) throws IOException;
}

package org.mvss.karta.framework.restclient;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.type.TypeFactory;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.mvss.karta.Constants;
import org.mvss.karta.dependencyinjection.enums.DataFormat;
import org.mvss.karta.dependencyinjection.utils.DataUtils;
import org.mvss.karta.dependencyinjection.utils.ParserUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

/**
 * Response must be closed manually by consumers.
 */
@Getter
@Log4j2
public class ApacheRestResponse implements RestResponse {
    private static final long serialVersionUID = 1L;

    private final String protocolVersion;
    private final int statusCode;
    private final String reasonPhrase;
    private final HashMap<String, String> headers;
    private ContentType contentType;

    private InputStream contentStream;

    public ApacheRestResponse(CloseableHttpResponse response) throws UnsupportedOperationException, IOException {
        StatusLine statusLine = response.getStatusLine();
        this.protocolVersion = statusLine.getProtocolVersion().toString();
        this.statusCode = statusLine.getStatusCode();
        this.reasonPhrase = statusLine.getReasonPhrase();

        this.headers = new HashMap<>();

        for (Header header : response.getAllHeaders()) {
            this.headers.put(header.getName(), header.getValue());

            if (header.getName().equals(Constants.CONTENT_TYPE)) {
                String contentTypeString = DataUtils.getContainedKey(header.getValue(), ContentType.getCONTENT_TYPE_MAP().keySet());

                if (contentTypeString != null) {
                    this.contentType = ContentType.getByMimeType(contentTypeString);
                }
            }
        }

        if (this.contentType == null) {
            this.contentType = ContentType.APPLICATION_JSON;
        }

        HttpEntity entity = response.getEntity();

        if (entity != null) {
            this.contentStream = entity.getContent();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getBodyAs(Class<T> type) throws IOException {
        if (contentStream == null) {
            return null;
        }

        if (type == InputStream.class) {
            return (T) contentStream;
        }

        byte[] body = contentStream.readAllBytes();

        if (type == byte[].class) {
            return (T) body;
        }

        if (type == String.class) {
            return (T) new String(body, StandardCharsets.UTF_8);
        }

        try {
            switch (contentType) {
                case APPLICATION_FORM_URLENCODED:
                case APPLICATION_OCTET_STREAM:
                case IMAGE_BMP:
                case IMAGE_GIF:
                case IMAGE_JPEG:
                case IMAGE_PNG:
                case IMAGE_SVG:
                case IMAGE_TIFF:
                case IMAGE_WEBP:
                case MULTIPART_FORM_DATA:
                    return ParserUtils.convertValue(DataFormat.JSON, body, type);

                case APPLICATION_XML:
                case APPLICATION_ATOM_XML:
                case APPLICATION_XHTML_XML:
                case APPLICATION_SOAP_XML:
                case APPLICATION_SVG_XML:
                    String bytesStr = new String(body);
                    return ParserUtils.readValue(DataFormat.XML, bytesStr, type);

                case APPLICATION_YML:
                case APPLICATION_YAML:
                case APPLICATION_X_YAML:
                case TEXT_YAML:
                    bytesStr = new String(body);
                    return ParserUtils.readValue(DataFormat.YAML, bytesStr, type);

                case TEXT_HTML:
                case TEXT_PLAIN:
                case TEXT_XML:
                case APPLICATION_JSON:
                default:
                    bytesStr = new String(body);
                    return ParserUtils.readValue(DataFormat.JSON, bytesStr, type);
            }
        } catch (Exception exception) {
            log.error("", exception);
            return null;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getBodyAs(TypeReference<T> type) throws IOException {
        return (T) getBodyAs(TypeFactory.rawClass(type.getType()));
    }

    @Override
    public void close() throws IOException {
        if (contentStream != null) {
            contentStream.close();
            contentStream = null;
        }
    }

    @Override
    public String getBody() throws IOException {
        return getBodyAs(String.class);
    }

    /**
     * Get the response stream typically to save as file.
     * The stream must be closed by consumers after use.
     */
    @Override
    public InputStream getStream() {
        return contentStream;
    }

    @Override
    public void downloadFile(File file) throws IOException {
        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            IOUtils.copy(contentStream, fileOutputStream);
        }
    }
}

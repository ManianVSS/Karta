package org.mvss.karta.framework.restclient;

import lombok.Getter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public enum ContentType {
    APPLICATION_ATOM_XML(MimeTypes.APPLICATION_ATOM_XML),
    APPLICATION_FORM_URLENCODED(MimeTypes.APPLICATION_FORM_URLENCODED),
    APPLICATION_JSON(MimeTypes.APPLICATION_JSON),
    APPLICATION_OCTET_STREAM(MimeTypes.APPLICATION_OCTET_STREAM),
    APPLICATION_SOAP_XML(MimeTypes.APPLICATION_SOAP_XML),
    APPLICATION_SVG_XML(MimeTypes.APPLICATION_SVG_XML),
    APPLICATION_XHTML_XML(MimeTypes.APPLICATION_XHTML_XML),
    APPLICATION_XML(MimeTypes.APPLICATION_XML),
    APPLICATION_YML(MimeTypes.APPLICATION_YML),
    APPLICATION_YAML(MimeTypes.APPLICATION_YAML),
    APPLICATION_X_YAML(MimeTypes.APPLICATION_X_YAML),

    IMAGE_BMP(MimeTypes.IMAGE_BMP),
    IMAGE_GIF(MimeTypes.IMAGE_GIF),
    IMAGE_JPEG(MimeTypes.IMAGE_JPEG),
    IMAGE_PNG(MimeTypes.IMAGE_PNG),
    IMAGE_SVG(MimeTypes.IMAGE_SVG),
    IMAGE_TIFF(MimeTypes.IMAGE_TIFF),
    IMAGE_WEBP(MimeTypes.IMAGE_WEBP),

    MULTIPART_FORM_DATA(MimeTypes.MULTIPART_FORM_DATA),

    TEXT_HTML(MimeTypes.TEXT_HTML),
    TEXT_PLAIN(MimeTypes.TEXT_PLAIN),
    TEXT_XML(MimeTypes.TEXT_XML),
    TEXT_YAML(MimeTypes.TEXT_YAML);

    @Getter
    private static final Map<String, ContentType> CONTENT_TYPE_MAP;

    static {

        final ContentType[] contentTypes = {APPLICATION_ATOM_XML, APPLICATION_FORM_URLENCODED, APPLICATION_JSON, APPLICATION_SVG_XML,
                APPLICATION_XHTML_XML, APPLICATION_XML, APPLICATION_YML, APPLICATION_YAML, APPLICATION_X_YAML,

                IMAGE_BMP, IMAGE_GIF, IMAGE_JPEG, IMAGE_PNG, IMAGE_SVG, IMAGE_TIFF, IMAGE_WEBP,

                MULTIPART_FORM_DATA,

                TEXT_HTML, TEXT_PLAIN, TEXT_XML, TEXT_YAML};
        final HashMap<String, ContentType> map = new HashMap<>();
        for (final ContentType contentType : contentTypes) {
            map.put(contentType.mimeType, contentType);
        }
        CONTENT_TYPE_MAP = Collections.unmodifiableMap(map);
    }

    public final String mimeType;

    ContentType(String mimeType) {
        this.mimeType = mimeType;
    }

    public static ContentType getByMimeType(final String mimeType) {
        if (mimeType == null) {
            return null;
        }
        return CONTENT_TYPE_MAP.get(mimeType);
    }

    public static class MimeTypes {
        public static String APPLICATION_ATOM_XML = "application/atom+xml";
        public static String APPLICATION_FORM_URLENCODED = "application/x-www-form-urlencoded";
        public static String APPLICATION_JSON = "application/json";
        public static String APPLICATION_OCTET_STREAM = "application/octet-stream";
        public static String APPLICATION_SOAP_XML = "application/soap+xml";
        public static String APPLICATION_SVG_XML = "application/svg+xml";
        public static String APPLICATION_XHTML_XML = "application/xhtml+xml";
        public static String APPLICATION_XML = "application/xml";
        public static String APPLICATION_YML = "application/yml";
        public static String APPLICATION_YAML = "application/yaml";
        public static String APPLICATION_X_YAML = "application/x-yaml";

        public static String IMAGE_BMP = "image/bmp";
        public static String IMAGE_GIF = "image/gif";
        public static String IMAGE_JPEG = "image/bmp";
        public static String IMAGE_PNG = "image/png";
        public static String IMAGE_SVG = "image/svg+xml";
        public static String IMAGE_TIFF = "image/tiff";
        public static String IMAGE_WEBP = "image/webp";

        public static String MULTIPART_FORM_DATA = "multipart/form-data";
        public static String TEXT_HTML = "text/html";
        public static String TEXT_PLAIN = "text/plain";
        public static String TEXT_XML = "text/xml";
        public static String TEXT_YAML = "text/yaml";

    }

}

package org.mvss.karta.dependencyinjection.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.CharacterData;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

public class XMLParser {

    public static final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

    public static Document readDocumentFromFile(String xmlFileName) throws ParserConfigurationException, IOException, SAXException {
        File file = new File(xmlFileName);
        return readDocument(file);
    }

    public static Document readDocument(File file) throws ParserConfigurationException, IOException, SAXException {
        return dbf.newDocumentBuilder().parse(file);
    }

    public static Document readDocument(String xmlString) throws ParserConfigurationException, IOException, SAXException {
        return dbf.newDocumentBuilder().parse(new InputSource(new StringReader(xmlString)));
    }


    public static Serializable readObjectFromFile(String fileName) throws IOException, ParserConfigurationException, SAXException {
        return readObject(new File(fileName));
    }

    public static Serializable readObject(File file) throws IOException, ParserConfigurationException, SAXException {
        return readObject(readDocument(file).getDocumentElement());
    }

    public static Serializable readObject(String xmlString) throws IOException, ParserConfigurationException, SAXException {
        return readObject(readDocument(xmlString).getDocumentElement());
    }


    public static HashMap<String, Serializable> readAttributesAsObject(Element documentElement) throws JsonProcessingException {
        HashMap<String, Serializable> parsedObject = new HashMap<>();
        NamedNodeMap attributes = documentElement.getAttributes();
        for (int attrIndex = 0; attrIndex < attributes.getLength(); attrIndex++) {
            Node attribute = attributes.item(attrIndex);
            //TODO: Replace objectMapper with a standalone object parser using regex which can identify value types.
            Serializable value = ParserUtils.readValue(attribute.getNodeValue(), Serializable.class);
            parsedObject.put(attribute.getNodeName(), value);
        }
        return parsedObject;
    }

    public static Serializable readObject(Element documentElement) throws JsonProcessingException {
        HashMap<String, Serializable> parsedObject = new HashMap<>();
        NodeList children = documentElement.getChildNodes();
        NamedNodeMap attributes = documentElement.getAttributes();
        for (int attrIndex = 0; attrIndex < attributes.getLength(); attrIndex++) {
            Node attribute = attributes.item(attrIndex);
            //TODO: Replace objectMapper with a standalone object parser using regex which can identify value types.
            Serializable value = ParserUtils.readValue(attribute.getNodeValue(), Serializable.class);
            parsedObject.put(attribute.getNodeName(), value);
        }
        for (int itr = 0; itr < children.getLength(); itr++) {
            Node childNode = children.item(itr);
            childNode.normalize();
            switch (childNode.getNodeType()) {
                // Attributes somehow don't make it in getChildNodes to switch case hence separate loop.
                case Node.ELEMENT_NODE:
                    Element childElement = (Element) childNode;
                    parsedObject.put(childElement.getNodeName(), readObject(childElement));
                    break;
                case Node.TEXT_NODE:
                case Node.CDATA_SECTION_NODE:
                    String text = (childNode.getNodeType() == Node.TEXT_NODE) ? ((Text) childNode).getWholeText() :
                            ((CharacterData) childNode).getData();
                    if (StringUtils.isNotBlank(text)) {
                        Serializable objectToMerge = ParserUtils.readValue(text, Serializable.class);

                        if (objectToMerge instanceof Map) {
                            //noinspection unchecked
                            parsedObject.putAll((Map<String, Serializable>) objectToMerge);
                        } else {
                            if (parsedObject.keySet().size() == 0) {
                                //Return the parsed object as Serializable value if passing
                                return objectToMerge;
                            } else {
                                //Assume
                                parsedObject.put("__text__", objectToMerge);
                            }
                        }
                    }
                    break;
            }
        }
        return parsedObject;
    }

    public static Serializable readFieldFromElement(Element element,
                                                    String name, Class<? extends Serializable> mappingClassType)
            throws JsonProcessingException {
        if ((element == null) || StringUtils.isBlank(name)) {
            return null;
        }

        Serializable objectRead = null;

        if (mappingClassType == null) {
            mappingClassType = Serializable.class;
        }

        NodeList childElementList = element.getElementsByTagName(name);

        if (childElementList.getLength() > 0) {
            objectRead = readObject((Element) childElementList.item(childElementList.getLength() - 1));
        } else if (element.hasAttribute(name)) {
            objectRead = ParserUtils.readValue(element.getAttribute(name), mappingClassType);
        }

        return objectRead;
    }

    public static String writeObject(String name, Serializable object) throws JsonProcessingException {
        return new XmlMapper().writerWithDefaultPrettyPrinter().writeValueAsString(object);
    }
}

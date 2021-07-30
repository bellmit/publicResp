package com.insta.hms.malaffi;

import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.core.xml.schema.XSString;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeValue;
import org.opensaml.saml.saml2.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import java.io.StringWriter;

import javax.xml.namespace.QName;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class SamlUtil {

  private static final Logger logger = LoggerFactory.getLogger(SamlUtil.class);

  public static XMLObject buildXmlObject(QName objectName) {
    return XMLObjectProviderRegistrySupport.getBuilderFactory().getBuilder(objectName)
        .buildObject(objectName);
  }

  public static XMLObject buildXmlObject(QName objectName, QName schemaType) {
    return XMLObjectProviderRegistrySupport.getBuilderFactory().getBuilder(schemaType)
        .buildObject(objectName, schemaType);
  }

  public static Attribute buildStringAttribute(String name, String value) {
    Attribute attribute = (Attribute) SamlUtil.buildXmlObject(Attribute.DEFAULT_ELEMENT_NAME);
    attribute.setName(name);

    XSString attrValue = (XSString) SamlUtil.buildXmlObject(AttributeValue.DEFAULT_ELEMENT_NAME,
        XSString.TYPE_NAME);
    attrValue.setValue(value);

    attribute.getAttributeValues().add(attrValue);
    return attribute;
  }

  public static Element marshallResponse(Response response) {
    try {
      return XMLObjectProviderRegistrySupport.getMarshallerFactory().getMarshaller(response)
          .marshall(response);
    } catch (MarshallingException marshallingException) {
      logger.error("Couldn't marshall SAML response", marshallingException);
    }
    return null;
  }

  public static String stringify(Element element) {
    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    Transformer transformer;
    try {
      transformer = transformerFactory.newTransformer();
      StringWriter stringWriter = new StringWriter();
      transformer.transform(new DOMSource(element), new StreamResult(stringWriter));
      return stringWriter.toString();
    } catch (TransformerException transformerException) {
      logger.error("Couldn't stringify xml", transformerException);
    }
    return null;
  }

}

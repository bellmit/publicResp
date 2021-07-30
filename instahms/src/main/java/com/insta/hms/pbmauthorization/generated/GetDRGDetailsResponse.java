
package com.insta.hms.pbmauthorization.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="GetDRGDetailsResult" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="xmlDRGDetails" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="auditFileContent" type="{http://www.w3.org/2001/XMLSchema}base64Binary" minOccurs="0"/>
 *         &lt;element name="reportFileContent" type="{http://www.w3.org/2001/XMLSchema}base64Binary" minOccurs="0"/>
 *         &lt;element name="errorMessage" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "getDRGDetailsResult",
    "xmlDRGDetails",
    "auditFileContent",
    "reportFileContent",
    "errorMessage"
})
@XmlRootElement(name = "GetDRGDetailsResponse")
public class GetDRGDetailsResponse {

    @XmlElement(name = "GetDRGDetailsResult")
    protected int getDRGDetailsResult;
    protected String xmlDRGDetails;
    protected byte[] auditFileContent;
    protected byte[] reportFileContent;
    protected String errorMessage;

    /**
     * Gets the value of the getDRGDetailsResult property.
     * 
     */
    public int getGetDRGDetailsResult() {
        return getDRGDetailsResult;
    }

    /**
     * Sets the value of the getDRGDetailsResult property.
     * 
     */
    public void setGetDRGDetailsResult(int value) {
        this.getDRGDetailsResult = value;
    }

    /**
     * Gets the value of the xmlDRGDetails property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getXmlDRGDetails() {
        return xmlDRGDetails;
    }

    /**
     * Sets the value of the xmlDRGDetails property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setXmlDRGDetails(String value) {
        this.xmlDRGDetails = value;
    }

    /**
     * Gets the value of the auditFileContent property.
     * 
     * @return
     *     possible object is
     *     byte[]
     */
    public byte[] getAuditFileContent() {
        return auditFileContent;
    }

    /**
     * Sets the value of the auditFileContent property.
     * 
     * @param value
     *     allowed object is
     *     byte[]
     */
    public void setAuditFileContent(byte[] value) {
        this.auditFileContent = ((byte[]) value);
    }

    /**
     * Gets the value of the reportFileContent property.
     * 
     * @return
     *     possible object is
     *     byte[]
     */
    public byte[] getReportFileContent() {
        return reportFileContent;
    }

    /**
     * Sets the value of the reportFileContent property.
     * 
     * @param value
     *     allowed object is
     *     byte[]
     */
    public void setReportFileContent(byte[] value) {
        this.reportFileContent = ((byte[]) value);
    }

    /**
     * Gets the value of the errorMessage property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Sets the value of the errorMessage property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setErrorMessage(String value) {
        this.errorMessage = value;
    }

}

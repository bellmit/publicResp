
package com.insta.hms.erxprescription.generated;

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
 *         &lt;element name="UploadERxAuthorizationResult" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="errorMessage" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="errorReport" type="{http://www.w3.org/2001/XMLSchema}base64Binary" minOccurs="0"/>
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
    "uploadERxAuthorizationResult",
    "errorMessage",
    "errorReport"
})
@XmlRootElement(name = "UploadERxAuthorizationResponse")
public class UploadERxAuthorizationResponse {

    @XmlElement(name = "UploadERxAuthorizationResult")
    protected int uploadERxAuthorizationResult;
    protected String errorMessage;
    protected byte[] errorReport;

    /**
     * Gets the value of the uploadERxAuthorizationResult property.
     * 
     */
    public int getUploadERxAuthorizationResult() {
        return uploadERxAuthorizationResult;
    }

    /**
     * Sets the value of the uploadERxAuthorizationResult property.
     * 
     */
    public void setUploadERxAuthorizationResult(int value) {
        this.uploadERxAuthorizationResult = value;
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

    /**
     * Gets the value of the errorReport property.
     * 
     * @return
     *     possible object is
     *     byte[]
     */
    public byte[] getErrorReport() {
        return errorReport;
    }

    /**
     * Sets the value of the errorReport property.
     * 
     * @param value
     *     allowed object is
     *     byte[]
     */
    public void setErrorReport(byte[] value) {
        this.errorReport = ((byte[]) value);
    }

}

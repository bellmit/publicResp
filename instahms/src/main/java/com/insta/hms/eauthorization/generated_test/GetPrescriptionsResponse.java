
package com.insta.hms.eauthorization.generated_test;

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
 *         &lt;element name="GetPrescriptionsResult" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="prescription" type="{http://www.w3.org/2001/XMLSchema}base64Binary" minOccurs="0"/>
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
    "getPrescriptionsResult",
    "prescription",
    "errorMessage"
})
@XmlRootElement(name = "GetPrescriptionsResponse")
public class GetPrescriptionsResponse {

    @XmlElement(name = "GetPrescriptionsResult")
    protected int getPrescriptionsResult;
    protected byte[] prescription;
    protected String errorMessage;

    /**
     * Gets the value of the getPrescriptionsResult property.
     * 
     */
    public int getGetPrescriptionsResult() {
        return getPrescriptionsResult;
    }

    /**
     * Sets the value of the getPrescriptionsResult property.
     * 
     */
    public void setGetPrescriptionsResult(int value) {
        this.getPrescriptionsResult = value;
    }

    /**
     * Gets the value of the prescription property.
     * 
     * @return
     *     possible object is
     *     byte[]
     */
    public byte[] getPrescription() {
        return prescription;
    }

    /**
     * Sets the value of the prescription property.
     * 
     * @param value
     *     allowed object is
     *     byte[]
     */
    public void setPrescription(byte[] value) {
        this.prescription = ((byte[]) value);
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

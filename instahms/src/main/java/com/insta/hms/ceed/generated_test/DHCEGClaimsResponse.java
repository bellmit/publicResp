
package com.insta.hms.ceed.generated_test;

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
 *         &lt;element name="DHCEG_ClaimsResult" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element ref="{}response" minOccurs="0"/>
 *         &lt;element name="infoMessage" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
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
    "dhcegClaimsResult",
    "response",
    "infoMessage"
})
@XmlRootElement(name = "DHCEG_ClaimsResponse")
public class DHCEGClaimsResponse {

    @XmlElement(name = "DHCEG_ClaimsResult")
    protected int dhcegClaimsResult;
    @XmlElement(namespace = "")
    protected Response response;
    protected String infoMessage;

    /**
     * Gets the value of the dhcegClaimsResult property.
     * 
     */
    public int getDHCEGClaimsResult() {
        return dhcegClaimsResult;
    }

    /**
     * Sets the value of the dhcegClaimsResult property.
     * 
     */
    public void setDHCEGClaimsResult(int value) {
        this.dhcegClaimsResult = value;
    }

    /**
     * Gets the value of the response property.
     * 
     * @return
     *     possible object is
     *     {@link Response }
     *     
     */
    public Response getResponse() {
        return response;
    }

    /**
     * Sets the value of the response property.
     * 
     * @param value
     *     allowed object is
     *     {@link Response }
     *     
     */
    public void setResponse(Response value) {
        this.response = value;
    }

    /**
     * Gets the value of the infoMessage property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getInfoMessage() {
        return infoMessage;
    }

    /**
     * Sets the value of the infoMessage property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setInfoMessage(String value) {
        this.infoMessage = value;
    }

}

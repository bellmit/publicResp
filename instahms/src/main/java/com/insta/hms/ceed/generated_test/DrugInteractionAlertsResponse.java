
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
 *         &lt;element name="DrugInteractionAlertsResult" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
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
    "drugInteractionAlertsResult"
})
@XmlRootElement(name = "DrugInteractionAlertsResponse")
public class DrugInteractionAlertsResponse {

    @XmlElement(name = "DrugInteractionAlertsResult")
    protected String drugInteractionAlertsResult;

    /**
     * Gets the value of the drugInteractionAlertsResult property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDrugInteractionAlertsResult() {
        return drugInteractionAlertsResult;
    }

    /**
     * Sets the value of the drugInteractionAlertsResult property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDrugInteractionAlertsResult(String value) {
        this.drugInteractionAlertsResult = value;
    }

}


package com.insta.hms.sso.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for LogoutOutput complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="LogoutOutput">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="TimestampOfLastLogin" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "LogoutOutput", propOrder = {
    "timestampOfLastLogin"
})
public class LogoutOutput {

    @XmlElement(name = "TimestampOfLastLogin", required = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar timestampOfLastLogin;

    /**
     * Gets the value of the timestampOfLastLogin property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getTimestampOfLastLogin() {
        return timestampOfLastLogin;
    }

    /**
     * Sets the value of the timestampOfLastLogin property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setTimestampOfLastLogin(XMLGregorianCalendar value) {
        this.timestampOfLastLogin = value;
    }

}

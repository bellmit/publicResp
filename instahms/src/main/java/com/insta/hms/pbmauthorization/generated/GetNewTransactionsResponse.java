
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
 *         &lt;element name="GetNewTransactionsResult" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="xmlTransactions" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
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
    "getNewTransactionsResult",
    "xmlTransactions",
    "errorMessage"
})
@XmlRootElement(name = "GetNewTransactionsResponse")
public class GetNewTransactionsResponse {

    @XmlElement(name = "GetNewTransactionsResult")
    protected int getNewTransactionsResult;
    protected String xmlTransactions;
    protected String errorMessage;

    /**
     * Gets the value of the getNewTransactionsResult property.
     * 
     */
    public int getGetNewTransactionsResult() {
        return getNewTransactionsResult;
    }

    /**
     * Sets the value of the getNewTransactionsResult property.
     * 
     */
    public void setGetNewTransactionsResult(int value) {
        this.getNewTransactionsResult = value;
    }

    /**
     * Gets the value of the xmlTransactions property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getXmlTransactions() {
        return xmlTransactions;
    }

    /**
     * Sets the value of the xmlTransactions property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setXmlTransactions(String value) {
        this.xmlTransactions = value;
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

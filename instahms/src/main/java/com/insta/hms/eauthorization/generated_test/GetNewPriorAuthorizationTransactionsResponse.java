
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
 *         &lt;element name="GetNewPriorAuthorizationTransactionsResult" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="foundTransactions" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
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
    "getNewPriorAuthorizationTransactionsResult",
    "foundTransactions",
    "errorMessage"
})
@XmlRootElement(name = "GetNewPriorAuthorizationTransactionsResponse")
public class GetNewPriorAuthorizationTransactionsResponse {

    @XmlElement(name = "GetNewPriorAuthorizationTransactionsResult")
    protected int getNewPriorAuthorizationTransactionsResult;
    protected String foundTransactions;
    protected String errorMessage;

    /**
     * Gets the value of the getNewPriorAuthorizationTransactionsResult property.
     * 
     */
    public int getGetNewPriorAuthorizationTransactionsResult() {
        return getNewPriorAuthorizationTransactionsResult;
    }

    /**
     * Sets the value of the getNewPriorAuthorizationTransactionsResult property.
     * 
     */
    public void setGetNewPriorAuthorizationTransactionsResult(int value) {
        this.getNewPriorAuthorizationTransactionsResult = value;
    }

    /**
     * Gets the value of the foundTransactions property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFoundTransactions() {
        return foundTransactions;
    }

    /**
     * Sets the value of the foundTransactions property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFoundTransactions(String value) {
        this.foundTransactions = value;
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


package com.insta.hms.dhaeclaimprescription.generated;

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
 *         &lt;element name="SearchTransactionsResult" type="{http://www.w3.org/2001/XMLSchema}int"/>
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
    "searchTransactionsResult",
    "foundTransactions",
    "errorMessage"
})
@XmlRootElement(name = "SearchTransactionsResponse")
public class SearchTransactionsResponse {

    @XmlElement(name = "SearchTransactionsResult")
    protected int searchTransactionsResult;
    protected String foundTransactions;
    protected String errorMessage;

    /**
     * Gets the value of the searchTransactionsResult property.
     * 
     */
    public int getSearchTransactionsResult() {
        return searchTransactionsResult;
    }

    /**
     * Sets the value of the searchTransactionsResult property.
     * 
     */
    public void setSearchTransactionsResult(int value) {
        this.searchTransactionsResult = value;
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

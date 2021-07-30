
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
 *         &lt;element name="login" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="pwd" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="direction" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="callerLicense" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ePartner" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="transactionID" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="TransactionStatus" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="transactionFileName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="transactionFromDate" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="transactionToDate" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="minRecordCount" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="maxRecordCount" type="{http://www.w3.org/2001/XMLSchema}int"/>
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
    "login",
    "pwd",
    "direction",
    "callerLicense",
    "ePartner",
    "transactionID",
    "transactionStatus",
    "transactionFileName",
    "transactionFromDate",
    "transactionToDate",
    "minRecordCount",
    "maxRecordCount"
})
@XmlRootElement(name = "SearchTransactions")
public class SearchTransactions {

    protected String login;
    protected String pwd;
    protected int direction;
    protected String callerLicense;
    protected String ePartner;
    protected int transactionID;
    @XmlElement(name = "TransactionStatus")
    protected int transactionStatus;
    protected String transactionFileName;
    protected String transactionFromDate;
    protected String transactionToDate;
    protected int minRecordCount;
    protected int maxRecordCount;

    /**
     * Gets the value of the login property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLogin() {
        return login;
    }

    /**
     * Sets the value of the login property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLogin(String value) {
        this.login = value;
    }

    /**
     * Gets the value of the pwd property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPwd() {
        return pwd;
    }

    /**
     * Sets the value of the pwd property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPwd(String value) {
        this.pwd = value;
    }

    /**
     * Gets the value of the direction property.
     * 
     */
    public int getDirection() {
        return direction;
    }

    /**
     * Sets the value of the direction property.
     * 
     */
    public void setDirection(int value) {
        this.direction = value;
    }

    /**
     * Gets the value of the callerLicense property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCallerLicense() {
        return callerLicense;
    }

    /**
     * Sets the value of the callerLicense property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCallerLicense(String value) {
        this.callerLicense = value;
    }

    /**
     * Gets the value of the ePartner property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEPartner() {
        return ePartner;
    }

    /**
     * Sets the value of the ePartner property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEPartner(String value) {
        this.ePartner = value;
    }

    /**
     * Gets the value of the transactionID property.
     * 
     */
    public int getTransactionID() {
        return transactionID;
    }

    /**
     * Sets the value of the transactionID property.
     * 
     */
    public void setTransactionID(int value) {
        this.transactionID = value;
    }

    /**
     * Gets the value of the transactionStatus property.
     * 
     */
    public int getTransactionStatus() {
        return transactionStatus;
    }

    /**
     * Sets the value of the transactionStatus property.
     * 
     */
    public void setTransactionStatus(int value) {
        this.transactionStatus = value;
    }

    /**
     * Gets the value of the transactionFileName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTransactionFileName() {
        return transactionFileName;
    }

    /**
     * Sets the value of the transactionFileName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTransactionFileName(String value) {
        this.transactionFileName = value;
    }

    /**
     * Gets the value of the transactionFromDate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTransactionFromDate() {
        return transactionFromDate;
    }

    /**
     * Sets the value of the transactionFromDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTransactionFromDate(String value) {
        this.transactionFromDate = value;
    }

    /**
     * Gets the value of the transactionToDate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTransactionToDate() {
        return transactionToDate;
    }

    /**
     * Sets the value of the transactionToDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTransactionToDate(String value) {
        this.transactionToDate = value;
    }

    /**
     * Gets the value of the minRecordCount property.
     * 
     */
    public int getMinRecordCount() {
        return minRecordCount;
    }

    /**
     * Sets the value of the minRecordCount property.
     * 
     */
    public void setMinRecordCount(int value) {
        this.minRecordCount = value;
    }

    /**
     * Gets the value of the maxRecordCount property.
     * 
     */
    public int getMaxRecordCount() {
        return maxRecordCount;
    }

    /**
     * Sets the value of the maxRecordCount property.
     * 
     */
    public void setMaxRecordCount(int value) {
        this.maxRecordCount = value;
    }

}
